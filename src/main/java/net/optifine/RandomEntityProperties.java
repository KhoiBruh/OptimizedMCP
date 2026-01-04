package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

public class RandomEntityProperties {
    public ResourceLocation[] resourceLocations = null;
    public RandomEntityRule[] rules = null;

    public ResourceLocation getTextureLocation(ResourceLocation loc, IRandomEntity randomEntity) {
        if (this.rules != null) {
            for (RandomEntityRule randomentityrule : this.rules) {
                if (randomentityrule.matches(randomEntity)) {
                    return randomentityrule.getTextureLocation(loc, randomEntity.getId());
                }
            }
        }

        if (this.resourceLocations != null) {
            int j = randomEntity.getId();
            int k = j % this.resourceLocations.length;
            return this.resourceLocations[k];
        } else {
            return loc;
        }
    }

    public boolean isValid(String path) {
        if (this.resourceLocations == null && this.rules == null) {
            Config.warn("No skins specified: " + path);
            return false;
        } else {
            if (this.rules != null) {
                for (RandomEntityRule randomentityrule : this.rules) {
                    if (!randomentityrule.isValid(path)) {
                        return false;
                    }
                }
            }

            if (this.resourceLocations != null) {
                for (ResourceLocation resourcelocation : this.resourceLocations) {
                    if (!Config.hasResource(resourcelocation)) {
                        Config.warn("Texture not found: " + resourcelocation.getResourcePath());
                        return false;
                    }
                }
            }

            return true;
        }
    }

}
