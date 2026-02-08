package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.optifine.CustomBlockLayers;
import net.optifine.override.ChunkCacheOF;
import net.optifine.render.AabbFrame;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class RenderChunk {
    public static final EnumWorldBlockLayer[] ENUM_WORLD_BLOCK_LAYERS = EnumWorldBlockLayer.values();
    public static int renderChunksUpdated;
    private final World world;
    private final RenderGlobal renderGlobal;
    private final ReentrantLock lockCompileTask = new ReentrantLock();
    private final ReentrantLock lockCompiledChunk = new ReentrantLock();
    private final Set<TileEntity> setTileEntities = new HashSet<>();
    private final int index;
    private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final VertexBuffer[] vertexBuffers = new VertexBuffer[EnumWorldBlockLayer.values().length];
    private final EnumWorldBlockLayer[] blockLayersSingle = new EnumWorldBlockLayer[1];
    private final boolean isMipmaps = Config.isMipmaps();
    private final boolean fixBlockLayer = true;
    private final RenderChunk[] renderChunksOfset16 = new RenderChunk[6];
    public CompiledChunk compiledChunk = CompiledChunk.DUMMY;
    public AxisAlignedBB boundingBox;
    public int regionX;
    public int regionZ;
    public AabbFrame boundingBoxParent;
    private BlockPos position;
    private ChunkCompileTaskGenerator compileTask = null;
    private int frameIndex = -1;
    private boolean needsUpdate = true;
    private final EnumMap<EnumFacing, BlockPos> mapEnumFacing = null;
    private final BlockPos[] positionOffsets16 = new BlockPos[EnumFacing.VALUES.length];
    private boolean playerUpdate = false;
    private boolean renderChunksOffset16Updated = false;
    private Chunk chunk;
    private final RenderChunk[] renderChunkNeighbours = new RenderChunk[EnumFacing.VALUES.length];
    private final RenderChunk[] renderChunkNeighboursValid = new RenderChunk[EnumFacing.VALUES.length];
    private boolean renderChunkNeighboursUpated = false;
    private final RenderGlobal.ContainerLocalRenderInformation renderInfo = new RenderGlobal.ContainerLocalRenderInformation(
            this, null, 0);

    public RenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos blockPosIn, int indexIn) {
        world = worldIn;
        renderGlobal = renderGlobalIn;
        index = indexIn;

        setPosition(blockPosIn);

        if (OpenGlHelper.useVbo()) {
            for (int i = 0; i < EnumWorldBlockLayer.values().length; ++i) {
                vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            }
        }
    }

    public boolean setFrameIndex(int frameIndexIn) {
        if (frameIndex == frameIndexIn) {
            return false;
        } else {
            frameIndex = frameIndexIn;
            return true;
        }
    }

    public VertexBuffer getVertexBufferByLayer(int layer) {
        return vertexBuffers[layer];
    }

    public void resortTransparency(float x, float y, float z, ChunkCompileTaskGenerator generator) {
        CompiledChunk compiledchunk = generator.getCompiledChunk();

        if (compiledchunk.getState() != null && !compiledchunk.isLayerEmpty(EnumWorldBlockLayer.TRANSLUCENT)) {
            WorldRenderer worldrenderer = generator.getRegionRenderCacheBuilder()
                    .getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT);
            preRenderBlocks(worldrenderer, position);
            worldrenderer.setVertexState(compiledchunk.getState());
            postRenderBlocks(EnumWorldBlockLayer.TRANSLUCENT, x, y, z, worldrenderer, compiledchunk);
        }
    }

    public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator) {
        CompiledChunk compiledchunk = new CompiledChunk();
        int i = 1;
        BlockPos blockpos = new BlockPos(position);
        BlockPos blockpos1 = blockpos.add(15, 15, 15);
        generator.getLock().lock();

        try {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
                return;
            }

            generator.setCompiledChunk(compiledchunk);
        } finally {
            generator.getLock().unlock();
        }

        VisGraph lvt_10_1_ = new VisGraph();
        HashSet<TileEntity> lvt_11_1_ = new HashSet<>();

        if (!isChunkRegionEmpty(blockpos)) {
            ++renderChunksUpdated;
            ChunkCacheOF chunkcacheof = makeChunkCacheOF(blockpos);
            chunkcacheof.renderStart();
            boolean[] aboolean = new boolean[ENUM_WORLD_BLOCK_LAYERS.length];
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            boolean flag = false;
            boolean flag1 = false;

            for (BlockPos blockposm : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {
                IBlockState iblockstate = chunkcacheof.getBlockState(blockposm);
                Block block = iblockstate.getBlock();

                if (block.isOpaqueCube()) {
                    lvt_10_1_.func_178606_a(blockposm);
                }

                if (iblockstate.getBlock().hasTileEntity()) {
                    TileEntity tileentity = chunkcacheof.getTileEntity(new BlockPos(blockposm));
                    TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = TileEntityRendererDispatcher.instance
                            .getSpecialRenderer(tileentity);

                    if (tileentity != null && tileentityspecialrenderer != null) {
                        compiledchunk.addTileEntity(tileentity);

                        if (tileentityspecialrenderer.forceTileEntityRender()) {
                            lvt_11_1_.add(tileentity);
                        }
                    }
                }

                EnumWorldBlockLayer[] aenumworldblocklayer;

                aenumworldblocklayer = blockLayersSingle;
                aenumworldblocklayer[0] = block.getBlockLayer();

                {
                    EnumWorldBlockLayer enumworldblocklayer = block.getBlockLayer();

                    enumworldblocklayer = fixBlockLayer(iblockstate, enumworldblocklayer);
                    int k = enumworldblocklayer.ordinal();

                    if (block.getRenderType() != -1) {
                        WorldRenderer worldrenderer = generator.getRegionRenderCacheBuilder()
                                .getWorldRendererByLayerId(k);
                        worldrenderer.setBlockLayer(enumworldblocklayer);
                        RenderEnv renderenv = worldrenderer.getRenderEnv(iblockstate, blockposm);
                        renderenv.setRegionRenderCacheBuilder(generator.getRegionRenderCacheBuilder());

                        if (!compiledchunk.isLayerStarted(enumworldblocklayer)) {
                            compiledchunk.setLayerStarted(enumworldblocklayer);
                            preRenderBlocks(worldrenderer, blockpos);
                        }

                        aboolean[k] |= blockrendererdispatcher.renderBlock(iblockstate, blockposm, chunkcacheof,
                                worldrenderer);

                        if (renderenv.isOverlaysRendered()) {
                            postRenderOverlays(generator.getRegionRenderCacheBuilder(), compiledchunk, aboolean);
                            renderenv.setOverlaysRendered(false);
                        }
                    }
                }

            }

            for (EnumWorldBlockLayer enumworldblocklayer1 : ENUM_WORLD_BLOCK_LAYERS) {
                if (aboolean[enumworldblocklayer1.ordinal()]) {
                    compiledchunk.setLayerUsed(enumworldblocklayer1);
                }

                if (compiledchunk.isLayerStarted(enumworldblocklayer1)) {
                    if (Config.isShaders()) {
                        SVertexBuilder.calcNormalChunkLayer(
                                generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(enumworldblocklayer1));
                    }

                    WorldRenderer worldrenderer1 = generator.getRegionRenderCacheBuilder()
                            .getWorldRendererByLayer(enumworldblocklayer1);
                    postRenderBlocks(enumworldblocklayer1, x, y, z, worldrenderer1, compiledchunk);

                    if (worldrenderer1.animatedSprites != null) {
                        compiledchunk.setAnimatedSprites(enumworldblocklayer1,
                                (BitSet) worldrenderer1.animatedSprites.clone());
                    }
                } else {
                    compiledchunk.setAnimatedSprites(enumworldblocklayer1, null);
                }
            }

            chunkcacheof.renderFinish();
        }

        compiledchunk.setVisibility(lvt_10_1_.computeVisibility());
        lockCompileTask.lock();

        try {
            Set<TileEntity> set = Sets.newHashSet(lvt_11_1_);
            Set<TileEntity> set1 = Sets.newHashSet(setTileEntities);
            set.removeAll(setTileEntities);
            set1.removeAll(lvt_11_1_);
            setTileEntities.clear();
            setTileEntities.addAll(lvt_11_1_);
            renderGlobal.updateTileEntities(set1, set);
        } finally {
            lockCompileTask.unlock();
        }
    }

    protected void finishCompileTask() {
        lockCompileTask.lock();

        try {
            if (compileTask != null && compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
                compileTask.finish();
                compileTask = null;
            }
        } finally {
            lockCompileTask.unlock();
        }
    }

    public ReentrantLock getLockCompileTask() {
        return lockCompileTask;
    }

    public ChunkCompileTaskGenerator makeCompileTaskChunk() {
        lockCompileTask.lock();
        ChunkCompileTaskGenerator chunkcompiletaskgenerator;

        try {
            finishCompileTask();
            compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK);
            chunkcompiletaskgenerator = compileTask;
        } finally {
            lockCompileTask.unlock();
        }

        return chunkcompiletaskgenerator;
    }

    public ChunkCompileTaskGenerator makeCompileTaskTransparency() {
        lockCompileTask.lock();
        ChunkCompileTaskGenerator chunkcompiletaskgenerator1;

        try {
            if (compileTask != null && compileTask.getStatus() == ChunkCompileTaskGenerator.Status.PENDING) {
                return null;
            }

            if (compileTask != null && compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
                compileTask.finish();
                compileTask = null;
            }

            compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY);
            compileTask.setCompiledChunk(compiledChunk);
            chunkcompiletaskgenerator1 = compileTask;
        } finally {
            lockCompileTask.unlock();
        }

        return chunkcompiletaskgenerator1;
    }

    private void preRenderBlocks(WorldRenderer worldRendererIn, BlockPos pos) {
        worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);

        if (Config.isRenderRegions()) {
            int i = 8;
            int j;
            int k = pos.getY() >> i << i;
            int l;
            j = regionX;
            l = regionZ;
            worldRendererIn.setTranslation(-j, -k, -l);
        } else {
            worldRendererIn.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
        }
    }

    private void postRenderBlocks(EnumWorldBlockLayer layer, float x, float y, float z, WorldRenderer worldRendererIn,
                                  CompiledChunk compiledChunkIn) {
        if (layer == EnumWorldBlockLayer.TRANSLUCENT && !compiledChunkIn.isLayerEmpty(layer)) {
            worldRendererIn.sortVertexData(x, y, z);
            compiledChunkIn.setState(worldRendererIn.getVertexState());
        }

        worldRendererIn.finishDrawing();
    }

    private void initModelviewMatrix() {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        float f = 1.000001F;
        GlStateManager.translate(-8.0F, -8.0F, -8.0F);
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.getFloat(2982, modelviewMatrix);
        GlStateManager.popMatrix();
    }

    public void multModelviewMatrix() {
        GlStateManager.multMatrix(modelviewMatrix);
    }

    public CompiledChunk getCompiledChunk() {
        return compiledChunk;
    }

    public void setCompiledChunk(CompiledChunk compiledChunkIn) {
        lockCompiledChunk.lock();

        try {
            compiledChunk = compiledChunkIn;
        } finally {
            lockCompiledChunk.unlock();
        }
    }

    public void stopCompileTask() {
        finishCompileTask();
        compiledChunk = CompiledChunk.DUMMY;
    }

    public void deleteGlResources() {
        stopCompileTask();

        for (int i = 0; i < EnumWorldBlockLayer.values().length; ++i) {
            if (vertexBuffers[i] != null) {
                vertexBuffers[i].deleteGlBuffers();
            }
        }
    }

    public BlockPos getPosition() {
        return position;
    }

    public void setPosition(BlockPos pos) {
        stopCompileTask();
        position = pos;
        int i = 8;
        regionX = pos.getX() >> i << i;
        regionZ = pos.getZ() >> i << i;
        boundingBox = new AxisAlignedBB(pos, pos.add(16, 16, 16));
        initModelviewMatrix();

        Arrays.fill(positionOffsets16, null);

        renderChunksOffset16Updated = false;
        renderChunkNeighboursUpated = false;

        for (RenderChunk renderchunk : renderChunkNeighbours) {
            if (renderchunk != null) {
                renderchunk.renderChunkNeighboursUpated = false;
            }
        }

        chunk = null;
        boundingBoxParent = null;
    }

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdateIn) {
        needsUpdate = needsUpdateIn;

        if (needsUpdateIn) {
            if (isWorldPlayerUpdate()) {
                playerUpdate = true;
            }
        } else {
            playerUpdate = false;
        }
    }

    public BlockPos getBlockPosOffset16(EnumFacing p_181701_1_) {
        return getPositionOffset16(p_181701_1_);
    }

    public BlockPos getPositionOffset16(EnumFacing p_getPositionOffset16_1_) {
        int i = p_getPositionOffset16_1_.getIndex();
        BlockPos blockpos = positionOffsets16[i];

        if (blockpos == null) {
            blockpos = position.offset(p_getPositionOffset16_1_, 16);
            positionOffsets16[i] = blockpos;
        }

        return blockpos;
    }

    private boolean isWorldPlayerUpdate() {
        if (world instanceof WorldClient worldclient) {
            return worldclient.isPlayerUpdate();
        } else {
            return false;
        }
    }

    public boolean isPlayerUpdate() {
        return playerUpdate;
    }

    protected RegionRenderCache createRegionRenderCache(World p_createRegionRenderCache_1_,
                                                        BlockPos p_createRegionRenderCache_2_, BlockPos p_createRegionRenderCache_3_,
                                                        int p_createRegionRenderCache_4_) {
        return new RegionRenderCache(p_createRegionRenderCache_1_, p_createRegionRenderCache_2_,
                p_createRegionRenderCache_3_, p_createRegionRenderCache_4_);
    }

    private EnumWorldBlockLayer fixBlockLayer(IBlockState p_fixBlockLayer_1_, EnumWorldBlockLayer p_fixBlockLayer_2_) {
        if (CustomBlockLayers.isActive()) {
            EnumWorldBlockLayer enumworldblocklayer = CustomBlockLayers.getRenderLayer(p_fixBlockLayer_1_);

            if (enumworldblocklayer != null) {
                return enumworldblocklayer;
            }
        }

        if (!fixBlockLayer) {
            return p_fixBlockLayer_2_;
        } else {
            if (isMipmaps) {
                if (p_fixBlockLayer_2_ == EnumWorldBlockLayer.CUTOUT) {
                    Block block = p_fixBlockLayer_1_.getBlock();

                    if (block instanceof BlockRedstoneWire) {
                        return p_fixBlockLayer_2_;
                    }

                    if (block instanceof BlockCactus) {
                        return p_fixBlockLayer_2_;
                    }

                    return EnumWorldBlockLayer.CUTOUT_MIPPED;
                }
            } else if (p_fixBlockLayer_2_ == EnumWorldBlockLayer.CUTOUT_MIPPED) {
                return EnumWorldBlockLayer.CUTOUT;
            }

            return p_fixBlockLayer_2_;
        }
    }

    private void postRenderOverlays(RegionRenderCacheBuilder p_postRenderOverlays_1_,
                                    CompiledChunk p_postRenderOverlays_2_, boolean[] p_postRenderOverlays_3_) {
        postRenderOverlay(EnumWorldBlockLayer.CUTOUT, p_postRenderOverlays_1_, p_postRenderOverlays_2_,
                p_postRenderOverlays_3_);
        postRenderOverlay(EnumWorldBlockLayer.CUTOUT_MIPPED, p_postRenderOverlays_1_, p_postRenderOverlays_2_,
                p_postRenderOverlays_3_);
        postRenderOverlay(EnumWorldBlockLayer.TRANSLUCENT, p_postRenderOverlays_1_, p_postRenderOverlays_2_,
                p_postRenderOverlays_3_);
    }

    private void postRenderOverlay(EnumWorldBlockLayer p_postRenderOverlay_1_,
                                   RegionRenderCacheBuilder p_postRenderOverlay_2_, CompiledChunk p_postRenderOverlay_3_,
                                   boolean[] p_postRenderOverlay_4_) {
        WorldRenderer worldrenderer = p_postRenderOverlay_2_.getWorldRendererByLayer(p_postRenderOverlay_1_);

        if (worldrenderer.isDrawing()) {
            p_postRenderOverlay_3_.setLayerStarted(p_postRenderOverlay_1_);
            p_postRenderOverlay_4_[p_postRenderOverlay_1_.ordinal()] = true;
        }
    }

    private ChunkCacheOF makeChunkCacheOF(BlockPos p_makeChunkCacheOF_1_) {
        BlockPos blockpos = p_makeChunkCacheOF_1_.add(-1, -1, -1);
        BlockPos blockpos1 = p_makeChunkCacheOF_1_.add(16, 16, 16);
        ChunkCache chunkcache = createRegionRenderCache(world, blockpos, blockpos1, 1);

        return new ChunkCacheOF(chunkcache, blockpos, blockpos1, 1);
    }

    public RenderChunk getRenderChunkOffset16(ViewFrustum p_getRenderChunkOffset16_1_,
                                              EnumFacing p_getRenderChunkOffset16_2_) {
        if (!renderChunksOffset16Updated) {
            for (int i = 0; i < EnumFacing.VALUES.length; ++i) {
                EnumFacing enumfacing = EnumFacing.VALUES[i];
                BlockPos blockpos = getBlockPosOffset16(enumfacing);
                renderChunksOfset16[i] = p_getRenderChunkOffset16_1_.getRenderChunk(blockpos);
            }

            renderChunksOffset16Updated = true;
        }

        return renderChunksOfset16[p_getRenderChunkOffset16_2_.ordinal()];
    }

    public Chunk getChunk() {
        return getChunk(position);
    }

    private Chunk getChunk(BlockPos p_getChunk_1_) {
        Chunk chunk = this.chunk;

        if (chunk != null && chunk.isLoaded()) {
            return chunk;
        } else {
            chunk = world.getChunkFromBlockCoords(p_getChunk_1_);
            this.chunk = chunk;
            return chunk;
        }
    }

    public boolean isChunkRegionEmpty() {
        return isChunkRegionEmpty(position);
    }

    private boolean isChunkRegionEmpty(BlockPos p_isChunkRegionEmpty_1_) {
        int i = p_isChunkRegionEmpty_1_.getY();
        int j = i + 15;
        return getChunk(p_isChunkRegionEmpty_1_).getAreLevelsEmpty(i, j);
    }

    public void setRenderChunkNeighbour(EnumFacing p_setRenderChunkNeighbour_1_,
                                        RenderChunk p_setRenderChunkNeighbour_2_) {
        renderChunkNeighbours[p_setRenderChunkNeighbour_1_.ordinal()] = p_setRenderChunkNeighbour_2_;
        renderChunkNeighboursValid[p_setRenderChunkNeighbour_1_.ordinal()] = p_setRenderChunkNeighbour_2_;
    }

    public RenderChunk getRenderChunkNeighbour(EnumFacing p_getRenderChunkNeighbour_1_) {
        if (!renderChunkNeighboursUpated) {
            updateRenderChunkNeighboursValid();
        }

        return renderChunkNeighboursValid[p_getRenderChunkNeighbour_1_.ordinal()];
    }

    public RenderGlobal.ContainerLocalRenderInformation getRenderInfo() {
        return renderInfo;
    }

    private void updateRenderChunkNeighboursValid() {
        int i = position.getX();
        int j = position.getZ();
        int k = EnumFacing.NORTH.ordinal();
        int l = EnumFacing.SOUTH.ordinal();
        int i1 = EnumFacing.WEST.ordinal();
        int j1 = EnumFacing.EAST.ordinal();
        renderChunkNeighboursValid[k] = renderChunkNeighbours[k].position.getZ() == j - 16
                ? renderChunkNeighbours[k]
                : null;
        renderChunkNeighboursValid[l] = renderChunkNeighbours[l].position.getZ() == j + 16
                ? renderChunkNeighbours[l]
                : null;
        renderChunkNeighboursValid[i1] = renderChunkNeighbours[i1].position.getX() == i - 16
                ? renderChunkNeighbours[i1]
                : null;
        renderChunkNeighboursValid[j1] = renderChunkNeighbours[j1].position.getX() == i + 16
                ? renderChunkNeighbours[j1]
                : null;
        renderChunkNeighboursUpated = true;
    }

    public boolean isBoundingBoxInFrustum(ICamera p_isBoundingBoxInFrustum_1_, int p_isBoundingBoxInFrustum_2_) {
        return getBoundingBoxParent().isBoundingBoxInFrustumFully(p_isBoundingBoxInFrustum_1_,
                p_isBoundingBoxInFrustum_2_) || p_isBoundingBoxInFrustum_1_.isBoundingBoxInFrustum(boundingBox);
    }

    public AabbFrame getBoundingBoxParent() {
        if (boundingBoxParent == null) {
            BlockPos blockpos = position;
            int i = blockpos.getX();
            int j = blockpos.getY();
            int k = blockpos.getZ();
            int l = 5;
            int i1 = i >> l << l;
            int j1 = j >> l << l;
            int k1 = k >> l << l;

            if (i1 != i || j1 != j || k1 != k) {
                AabbFrame aabbframe = renderGlobal.getRenderChunk(new BlockPos(i1, j1, k1)).getBoundingBoxParent();

                if (aabbframe != null && aabbframe.minX == (double) i1 && aabbframe.minY == (double) j1
                        && aabbframe.minZ == (double) k1) {
                    boundingBoxParent = aabbframe;
                }
            }

            if (boundingBoxParent == null) {
                int l1 = 1 << l;
                boundingBoxParent = new AabbFrame(i1, j1, k1, i1 + l1,
                        j1 + l1, k1 + l1);
            }
        }

        return boundingBoxParent;
    }

    public String toString() {
        return "pos: " + position + ", frameIndex: " + frameIndex;
    }
}
