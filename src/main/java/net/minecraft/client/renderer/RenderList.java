package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.src.Config;
import net.minecraft.util.WorldBlockLayer;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class RenderList extends ChunkRenderContainer {
    IntBuffer bufferLists = GLAllocation.createDirectIntBuffer(16);
    private double viewEntityX;
    private double viewEntityY;
    private double viewEntityZ;

    public void renderChunkLayer(WorldBlockLayer layer) {
        if (initialized) {
            if (!Config.isRenderRegions()) {
                for (RenderChunk renderchunk1 : renderChunks) {
                    ListedRenderChunk listedrenderchunk1 = (ListedRenderChunk) renderchunk1;
                    GlStateManager.pushMatrix();
                    preRenderChunk(renderchunk1);
                    GL11.glCallList(listedrenderchunk1.getDisplayList(layer, listedrenderchunk1.getCompiledChunk()));
                    GlStateManager.popMatrix();
                }
            } else {
                int i = Integer.MIN_VALUE;
                int j = Integer.MIN_VALUE;

                for (RenderChunk renderchunk : renderChunks) {
                    ListedRenderChunk listedrenderchunk = (ListedRenderChunk) renderchunk;

                    if (i != renderchunk.regionX || j != renderchunk.regionZ) {
                        if (bufferLists.position() > 0) {
                            drawRegion(i, j, bufferLists);
                        }

                        i = renderchunk.regionX;
                        j = renderchunk.regionZ;
                    }

                    if (bufferLists.position() >= bufferLists.capacity()) {
                        IntBuffer intbuffer = GLAllocation.createDirectIntBuffer(bufferLists.capacity() * 2);
                        bufferLists.flip();
                        intbuffer.put(bufferLists);
                        bufferLists = intbuffer;
                    }

                    bufferLists.put(listedrenderchunk.getDisplayList(layer, listedrenderchunk.getCompiledChunk()));
                }

                if (bufferLists.position() > 0) {
                    drawRegion(i, j, bufferLists);
                }
            }

            if (Config.isMultiTexture()) {
                GlStateManager.bindCurrentTexture();
            }

            GlStateManager.resetColor();
            renderChunks.clear();
        }
    }

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn) {
        viewEntityX = viewEntityXIn;
        viewEntityY = viewEntityYIn;
        viewEntityZ = viewEntityZIn;
        super.initialize(viewEntityXIn, viewEntityYIn, viewEntityZIn);
    }

    private void drawRegion(int p_drawRegion_1_, int p_drawRegion_2_, IntBuffer p_drawRegion_3_) {
        GlStateManager.pushMatrix();
        preRenderRegion(p_drawRegion_1_, 0, p_drawRegion_2_);
        p_drawRegion_3_.flip();
        GlStateManager.callLists(p_drawRegion_3_);
        p_drawRegion_3_.clear();
        GlStateManager.popMatrix();
    }

    public void preRenderRegion(int p_preRenderRegion_1_, int p_preRenderRegion_2_, int p_preRenderRegion_3_) {
        GlStateManager.translate((float) ((double) p_preRenderRegion_1_ - viewEntityX), (float) ((double) p_preRenderRegion_2_ - viewEntityY), (float) ((double) p_preRenderRegion_3_ - viewEntityZ));
    }
}
