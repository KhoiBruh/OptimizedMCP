package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

public class RandomEntityProperties {
    public final ResourceLocation[] resourceLocations = null;
    public final RandomEntityRule[] rules = null;

    public ResourceLocation getTextureLocation(ResourceLocation loc, IRandomEntity randomEntity) {

        return loc;
    }

    public boolean isValid(String path) {
        Config.warn("No skins specified: " + path);
        return false;
    }

}
