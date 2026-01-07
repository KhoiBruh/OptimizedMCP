package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelSquid extends ModelBase {
    public ModelRenderer squidBody;
    public ModelRenderer[] squidTentacles = new ModelRenderer[8];

    public ModelSquid() {
        int i = -16;
        squidBody = new ModelRenderer(this, 0, 0);
        squidBody.addBox(-6.0F, -8.0F, -6.0F, 12, 16, 12);
        squidBody.rotationPointY += (float) (24 + i);

        for (int j = 0; j < squidTentacles.length; ++j) {
            squidTentacles[j] = new ModelRenderer(this, 48, 0);
            double d0 = (double) j * Math.PI * 2.0D / (double) squidTentacles.length;
            float f = (float) Math.cos(d0) * 5.0F;
            float f1 = (float) Math.sin(d0) * 5.0F;
            squidTentacles[j].addBox(-1.0F, 0.0F, -1.0F, 2, 18, 2);
            squidTentacles[j].rotationPointX = f;
            squidTentacles[j].rotationPointZ = f1;
            squidTentacles[j].rotationPointY = (float) (31 + i);
            d0 = (double) j * Math.PI * -2.0D / (double) squidTentacles.length + (Math.PI / 2D);
            squidTentacles[j].rotateAngleY = (float) d0;
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        for (ModelRenderer modelrenderer : squidTentacles) {
            modelrenderer.rotateAngleX = ageInTicks;
        }
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        squidBody.render(scale);

        for (ModelRenderer squidTentacle : squidTentacles) {
            squidTentacle.render(scale);
        }
    }
}
