package net.optifine.entity.model;

import net.minecraft.util.ResourceLocation;

public interface IEntityRenderer {
    Class getEntityClass();

    void setEntityClass(Class var1);

    void setLocationTextureCustom(ResourceLocation var1);
}
