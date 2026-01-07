package net.minecraft.client.renderer.entity;

import net.minecraft.block.material.Material;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.layers.LayerEndermanEyes;
import net.minecraft.client.renderer.entity.layers.LayerHeldBlock;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class RenderEnderman extends RenderLiving<EntityEnderman> {
    private static final ResourceLocation endermanTextures = new ResourceLocation("textures/entity/enderman/enderman.png");
    private final ModelEnderman endermanModel;
    private final Random rnd = new Random();

    public RenderEnderman(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelEnderman(0.0F), 0.5F);
        endermanModel = (ModelEnderman) super.mainModel;
        addLayer(new LayerEndermanEyes(this));
        addLayer(new LayerHeldBlock(this));
    }

    public void doRender(EntityEnderman entity, double x, double y, double z, float entityYaw, float partialTicks) {
        endermanModel.isCarrying = entity.getHeldBlockState().getBlock().getMaterial() != Material.air;
        endermanModel.isAttacking = entity.isScreaming();

        if (entity.isScreaming()) {
            double d0 = 0.02D;
            x += rnd.nextGaussian() * d0;
            z += rnd.nextGaussian() * d0;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityEnderman entity) {
        return endermanTextures;
    }
}
