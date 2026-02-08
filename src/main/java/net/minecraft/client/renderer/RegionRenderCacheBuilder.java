package net.minecraft.client.renderer;

import net.minecraft.util.WorldBlockLayer;

public class RegionRenderCacheBuilder {
    private final WorldRenderer[] worldRenderers = new WorldRenderer[WorldBlockLayer.values().length];

    public RegionRenderCacheBuilder() {
        worldRenderers[WorldBlockLayer.SOLID.ordinal()] = new WorldRenderer(2097152);
        worldRenderers[WorldBlockLayer.CUTOUT.ordinal()] = new WorldRenderer(131072);
        worldRenderers[WorldBlockLayer.CUTOUT_MIPPED.ordinal()] = new WorldRenderer(131072);
        worldRenderers[WorldBlockLayer.TRANSLUCENT.ordinal()] = new WorldRenderer(262144);
    }

    public WorldRenderer getWorldRendererByLayer(WorldBlockLayer layer) {
        return worldRenderers[layer.ordinal()];
    }

    public WorldRenderer getWorldRendererByLayerId(int id) {
        return worldRenderers[id];
    }
}
