package net.minecraft.util;

public enum WorldBlockLayer {
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String layerName;

    WorldBlockLayer(String layerNameIn) {
        layerName = layerNameIn;
    }

    public String toString() {
        return layerName;
    }
}
