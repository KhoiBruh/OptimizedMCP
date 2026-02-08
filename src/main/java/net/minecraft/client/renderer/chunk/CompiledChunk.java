package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.WorldBlockLayer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class CompiledChunk {
    public static final CompiledChunk DUMMY = new CompiledChunk() {
        protected void setLayerUsed(WorldBlockLayer layer) {
            throw new UnsupportedOperationException();
        }

        public void setLayerStarted(WorldBlockLayer layer) {
            throw new UnsupportedOperationException();
        }

        public boolean isVisible(Direction facing, Direction facing2) {
            return false;
        }

        public void setAnimatedSprites(WorldBlockLayer p_setAnimatedSprites_1_, BitSet p_setAnimatedSprites_2_) {
            throw new UnsupportedOperationException();
        }
    };
    private final boolean[] layersUsed = new boolean[RenderChunk.WORLD_BLOCK_LAYERS.length];
    private final boolean[] layersStarted = new boolean[RenderChunk.WORLD_BLOCK_LAYERS.length];
    private final List<TileEntity> tileEntities = new ArrayList<>();
    private boolean empty = true;
    private SetVisibility setVisibility = new SetVisibility();
    private WorldRenderer.State state;
    private final BitSet[] animatedSprites = new BitSet[RenderChunk.WORLD_BLOCK_LAYERS.length];

    public boolean isEmpty() {
        return empty;
    }

    protected void setLayerUsed(WorldBlockLayer layer) {
        empty = false;
        layersUsed[layer.ordinal()] = true;
    }

    public boolean isLayerEmpty(WorldBlockLayer layer) {
        return !layersUsed[layer.ordinal()];
    }

    public void setLayerStarted(WorldBlockLayer layer) {
        layersStarted[layer.ordinal()] = true;
    }

    public boolean isLayerStarted(WorldBlockLayer layer) {
        return layersStarted[layer.ordinal()];
    }

    public List<TileEntity> getTileEntities() {
        return tileEntities;
    }

    public void addTileEntity(TileEntity tileEntityIn) {
        tileEntities.add(tileEntityIn);
    }

    public boolean isVisible(Direction facing, Direction facing2) {
        return setVisibility.isVisible(facing, facing2);
    }

    public void setVisibility(SetVisibility visibility) {
        setVisibility = visibility;
    }

    public WorldRenderer.State getState() {
        return state;
    }

    public void setState(WorldRenderer.State stateIn) {
        state = stateIn;
    }

    public BitSet getAnimatedSprites(WorldBlockLayer p_getAnimatedSprites_1_) {
        return animatedSprites[p_getAnimatedSprites_1_.ordinal()];
    }

    public void setAnimatedSprites(WorldBlockLayer p_setAnimatedSprites_1_, BitSet p_setAnimatedSprites_2_) {
        animatedSprites[p_setAnimatedSprites_1_.ordinal()] = p_setAnimatedSprites_2_;
    }
}
