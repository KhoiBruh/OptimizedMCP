package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.optifine.SmartAnimations;

import java.util.BitSet;
import java.util.List;

public abstract class ChunkRenderContainer {
    private final BitSet animatedSpritesCached = new BitSet();
    protected List<RenderChunk> renderChunks = Lists.newArrayListWithCapacity(17424);
    protected boolean initialized;
    private double viewEntityX;
    private double viewEntityY;
    private double viewEntityZ;
    private BitSet animatedSpritesRendered;

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn) {
        initialized = true;
        renderChunks.clear();
        viewEntityX = viewEntityXIn;
        viewEntityY = viewEntityYIn;
        viewEntityZ = viewEntityZIn;

        if (SmartAnimations.isActive()) {
            if (animatedSpritesRendered != null) {
                SmartAnimations.spritesRendered(animatedSpritesRendered);
            } else {
                animatedSpritesRendered = animatedSpritesCached;
            }

            animatedSpritesRendered.clear();
        } else if (animatedSpritesRendered != null) {
            SmartAnimations.spritesRendered(animatedSpritesRendered);
            animatedSpritesRendered = null;
        }
    }

    public void preRenderChunk(RenderChunk renderChunkIn) {
        BlockPos blockpos = renderChunkIn.getPosition();
        GlStateManager.translate((float) ((double) blockpos.getX() - viewEntityX), (float) ((double) blockpos.getY() - viewEntityY), (float) ((double) blockpos.getZ() - viewEntityZ));
    }

    public void addRenderChunk(RenderChunk renderChunkIn, EnumWorldBlockLayer layer) {
        renderChunks.add(renderChunkIn);

        if (animatedSpritesRendered != null) {
            BitSet bitset = renderChunkIn.compiledChunk.getAnimatedSprites(layer);

            if (bitset != null) {
                animatedSpritesRendered.or(bitset);
            }
        }
    }

    public abstract void renderChunkLayer(EnumWorldBlockLayer layer);
}
