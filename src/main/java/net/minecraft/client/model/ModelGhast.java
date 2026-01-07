package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import java.util.Random;

public class ModelGhast extends ModelBase {
    public ModelRenderer body;
    public ModelRenderer[] tentacles = new ModelRenderer[9];

    public ModelGhast() {
        int i = -16;
        body = new ModelRenderer(this, 0, 0);
        body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
        body.rotationPointY += (float) (24 + i);
        Random random = new Random(1660L);

        for (int j = 0; j < tentacles.length; ++j) {
            tentacles[j] = new ModelRenderer(this, 0, 0);
            float f = (((float) (j % 3) - (float) (j / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float f1 = ((float) (j / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int k = random.nextInt(7) + 8;
            tentacles[j].addBox(-1.0F, 0.0F, -1.0F, 2, k, 2);
            tentacles[j].rotationPointX = f;
            tentacles[j].rotationPointZ = f1;
            tentacles[j].rotationPointY = (float) (31 + i);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        for (int i = 0; i < tentacles.length; ++i) {
            tentacles[i].rotateAngleX = 0.2F * MathHelper.sin(ageInTicks * 0.3F + (float) i) + 0.4F;
        }
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.6F, 0.0F);
        body.render(scale);

        for (ModelRenderer modelrenderer : tentacles) {
            modelrenderer.render(scale);
        }

        GlStateManager.popMatrix();
    }
}
