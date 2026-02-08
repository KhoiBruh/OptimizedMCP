package net.minecraft.world;

public enum SkyBlock {
    SKY(15),
    BLOCK(0);

    public final int defaultLightValue;

    SkyBlock(int p_i1961_3_) {
        defaultLightValue = p_i1961_3_;
    }
}
