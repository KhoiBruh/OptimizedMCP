package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.WorldBlockLayer;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ChunkRenderDispatcher {
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
    private final List<ChunkRenderWorker> listThreadedWorkers;
    private final BlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates;
    private final BlockingQueue<RegionRenderCacheBuilder> queueFreeRenderBuilders;
    private final WorldVertexBufferUploader worldVertexUploader;
    private final VertexBufferUploader vertexUploader;
    private final Queue<ListenableFutureTask<?>> queueChunkUploads;
    private final ChunkRenderWorker renderWorker;
    private final int countRenderBuilders;
    private final List<RegionRenderCacheBuilder> listPausedBuilders;

    public ChunkRenderDispatcher() {
        this(-1);
    }

    public ChunkRenderDispatcher(int p_i4_1_) {
        listThreadedWorkers = new ArrayList<>();
        queueChunkUpdates = Queues.newArrayBlockingQueue(100);
        worldVertexUploader = new WorldVertexBufferUploader();
        vertexUploader = new VertexBufferUploader();
        queueChunkUploads = Queues.newArrayDeque();
        listPausedBuilders = new ArrayList<>();
        int i = Math.max(1, (int) ((double) Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
        int j = Math.max(1, MathHelper.clamp_int(Runtime.getRuntime().availableProcessors() - 2, 1, i / 5));

        if (p_i4_1_ < 0) {
            countRenderBuilders = MathHelper.clamp_int(j * 8, 1, i);
        } else {
            countRenderBuilders = p_i4_1_;
        }

        for (int k = 0; k < j; ++k) {
            ChunkRenderWorker chunkrenderworker = new ChunkRenderWorker(this);
            Thread thread = threadFactory.newThread(chunkrenderworker);
            thread.start();
            listThreadedWorkers.add(chunkrenderworker);
        }

        queueFreeRenderBuilders = Queues.newArrayBlockingQueue(countRenderBuilders);

        for (int l = 0; l < countRenderBuilders; ++l) {
            queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
        }

        renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
    }

    public String getDebugInfo() {
        return String.format("pC: %03d, pU: %1d, aB: %1d", queueChunkUpdates.size(), queueChunkUploads.size(), queueFreeRenderBuilders.size());
    }

    public boolean runChunkUploads(long p_178516_1_) {
        boolean flag = false;

        while (true) {
            boolean flag1 = false;
            ListenableFutureTask listenablefuturetask;

            synchronized (queueChunkUploads) {
                listenablefuturetask = queueChunkUploads.poll();
            }

            if (listenablefuturetask != null) {
                listenablefuturetask.run();
                flag1 = true;
                flag = true;
            }

            if (p_178516_1_ == 0L || !flag1) {
                break;
            }

            long i = p_178516_1_ - System.nanoTime();

            if (i < 0L) {
                break;
            }
        }

        return flag;
    }

    public boolean updateChunkLater(RenderChunk chunkRenderer) {
        chunkRenderer.getLockCompileTask().lock();
        boolean flag;

        try {
            final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();
            chunkcompiletaskgenerator.addFinishRunnable(() -> queueChunkUpdates.remove(chunkcompiletaskgenerator));
            boolean flag1 = queueChunkUpdates.offer(chunkcompiletaskgenerator);

            if (!flag1) {
                chunkcompiletaskgenerator.finish();
            }

            flag = flag1;
        } finally {
            chunkRenderer.getLockCompileTask().unlock();
        }

        return flag;
    }

    public boolean updateChunkNow(RenderChunk chunkRenderer) {
        chunkRenderer.getLockCompileTask().lock();
        boolean flag;

        try {
            ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

            try {
                renderWorker.processTask(chunkcompiletaskgenerator);
            } catch (InterruptedException var8) {
            }

            flag = true;
        } finally {
            chunkRenderer.getLockCompileTask().unlock();
        }

        return true;
    }

    public void stopChunkUpdates() {
        clearChunkUpdates();

        while (runChunkUploads(0L)) {
        }

        List<RegionRenderCacheBuilder> list = new ArrayList<>();

        while (list.size() != countRenderBuilders) {
            try {
                list.add(allocateRenderBuilder());
            } catch (InterruptedException var3) {
            }
        }

        queueFreeRenderBuilders.addAll(list);
    }

    public void freeRenderBuilder(RegionRenderCacheBuilder p_178512_1_) {
        queueFreeRenderBuilders.add(p_178512_1_);
    }

    public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
        return queueFreeRenderBuilders.take();
    }

    public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException {
        return queueChunkUpdates.take();
    }

    public boolean updateTransparencyLater(RenderChunk chunkRenderer) {
        chunkRenderer.getLockCompileTask().lock();

        try {
            final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskTransparency();

            if (chunkcompiletaskgenerator != null) {
                chunkcompiletaskgenerator.addFinishRunnable(() -> queueChunkUpdates.remove(chunkcompiletaskgenerator));
                return queueChunkUpdates.offer(chunkcompiletaskgenerator);
            }

        } finally {
            chunkRenderer.getLockCompileTask().unlock();
        }

        return true;
    }

    public ListenableFuture<Object> uploadChunk(final WorldBlockLayer player, final WorldRenderer p_178503_2_, final RenderChunk chunkRenderer, final CompiledChunk compiledChunkIn) {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            if (OpenGlHelper.useVbo()) {
                uploadVertexBuffer(p_178503_2_, chunkRenderer.getVertexBufferByLayer(player.ordinal()));
            } else {
                uploadDisplayList(p_178503_2_, ((ListedRenderChunk) chunkRenderer).getDisplayList(player, compiledChunkIn), chunkRenderer);
            }

            p_178503_2_.setTranslation(0, 0, 0);
            return Futures.immediateFuture(null);
        } else {
            ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.create(() -> uploadChunk(player, p_178503_2_, chunkRenderer, compiledChunkIn), null);

            synchronized (queueChunkUploads) {
                queueChunkUploads.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
    }

    private void uploadDisplayList(WorldRenderer p_178510_1_, int p_178510_2_, RenderChunk chunkRenderer) {
        GL11.glNewList(p_178510_2_, GL11.GL_COMPILE);
        GlStateManager.pushMatrix();
        chunkRenderer.multModelviewMatrix();
        worldVertexUploader.draw(p_178510_1_);
        GlStateManager.popMatrix();
        GL11.glEndList();
    }

    private void uploadVertexBuffer(WorldRenderer p_178506_1_, VertexBuffer vertexBufferIn) {
        vertexUploader.setVertexBuffer(vertexBufferIn);
        vertexUploader.draw(p_178506_1_);
    }

    public void clearChunkUpdates() {
        while (!queueChunkUpdates.isEmpty()) {
            ChunkCompileTaskGenerator chunkcompiletaskgenerator = queueChunkUpdates.poll();

            if (chunkcompiletaskgenerator != null) {
                chunkcompiletaskgenerator.finish();
            }
        }
    }

    public boolean hasChunkUpdates() {
        return queueChunkUpdates.isEmpty() && queueChunkUploads.isEmpty();
    }

    public void pauseChunkUpdates() {
        while (listPausedBuilders.size() != countRenderBuilders) {
            try {
                runChunkUploads(Long.MAX_VALUE);
                RegionRenderCacheBuilder regionrendercachebuilder = queueFreeRenderBuilders.poll(100L, TimeUnit.MILLISECONDS);

                if (regionrendercachebuilder != null) {
                    listPausedBuilders.add(regionrendercachebuilder);
                }
            } catch (InterruptedException var2) {
            }
        }
    }

    public void resumeChunkUpdates() {
        queueFreeRenderBuilders.addAll(listPausedBuilders);
        listPausedBuilders.clear();
    }
}
