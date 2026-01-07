package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.MathHelper;

public class ModelWolf extends ModelBase {
    public ModelRenderer wolfHeadMain;
    public ModelRenderer wolfBody;
    public ModelRenderer wolfLeg1;
    public ModelRenderer wolfLeg2;
    public ModelRenderer wolfLeg3;
    public ModelRenderer wolfLeg4;
    public ModelRenderer wolfTail;
    public ModelRenderer wolfMane;

    public ModelWolf() {
        float f = 0.0F;
        float f1 = 13.5F;
        wolfHeadMain = new ModelRenderer(this, 0, 0);
        wolfHeadMain.addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4, f);
        wolfHeadMain.setRotationPoint(-1.0F, f1, -7.0F);
        wolfBody = new ModelRenderer(this, 18, 14);
        wolfBody.addBox(-4.0F, -2.0F, -3.0F, 6, 9, 6, f);
        wolfBody.setRotationPoint(0.0F, 14.0F, 2.0F);
        wolfMane = new ModelRenderer(this, 21, 0);
        wolfMane.addBox(-4.0F, -3.0F, -3.0F, 8, 6, 7, f);
        wolfMane.setRotationPoint(-1.0F, 14.0F, 2.0F);
        wolfLeg1 = new ModelRenderer(this, 0, 18);
        wolfLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, f);
        wolfLeg1.setRotationPoint(-2.5F, 16.0F, 7.0F);
        wolfLeg2 = new ModelRenderer(this, 0, 18);
        wolfLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, f);
        wolfLeg2.setRotationPoint(0.5F, 16.0F, 7.0F);
        wolfLeg3 = new ModelRenderer(this, 0, 18);
        wolfLeg3.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, f);
        wolfLeg3.setRotationPoint(-2.5F, 16.0F, -4.0F);
        wolfLeg4 = new ModelRenderer(this, 0, 18);
        wolfLeg4.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, f);
        wolfLeg4.setRotationPoint(0.5F, 16.0F, -4.0F);
        wolfTail = new ModelRenderer(this, 9, 18);
        wolfTail.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, f);
        wolfTail.setRotationPoint(-1.0F, 12.0F, 8.0F);
        wolfHeadMain.setTextureOffset(16, 14).addBox(-3.0F, -5.0F, 0.0F, 2, 2, 1, f);
        wolfHeadMain.setTextureOffset(16, 14).addBox(1.0F, -5.0F, 0.0F, 2, 2, 1, f);
        wolfHeadMain.setTextureOffset(0, 10).addBox(-1.5F, 0.0F, -5.0F, 3, 3, 4, f);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);

        if (isChild) {
            float f = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 5.0F * scale, 2.0F * scale);
            wolfHeadMain.renderWithRotation(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            wolfBody.render(scale);
            wolfLeg1.render(scale);
            wolfLeg2.render(scale);
            wolfLeg3.render(scale);
            wolfLeg4.render(scale);
            wolfTail.renderWithRotation(scale);
            wolfMane.render(scale);
            GlStateManager.popMatrix();
        } else {
            wolfHeadMain.renderWithRotation(scale);
            wolfBody.render(scale);
            wolfLeg1.render(scale);
            wolfLeg2.render(scale);
            wolfLeg3.render(scale);
            wolfLeg4.render(scale);
            wolfTail.renderWithRotation(scale);
            wolfMane.render(scale);
        }
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_,
                                    float partialTickTime) {
        EntityWolf entitywolf = (EntityWolf) entitylivingbaseIn;

        if (entitywolf.isAngry()) {
            wolfTail.rotateAngleY = 0.0F;
        } else {
            wolfTail.rotateAngleY = MathHelper.cos(p_78086_2_ * 0.6662F) * 1.4F * p_78086_3_;
        }

        if (entitywolf.isSitting()) {
            wolfMane.setRotationPoint(-1.0F, 16.0F, -3.0F);
            wolfMane.rotateAngleX = ((float) Math.PI * 2F / 5F);
            wolfMane.rotateAngleY = 0.0F;
            wolfBody.setRotationPoint(0.0F, 18.0F, 0.0F);
            wolfBody.rotateAngleX = ((float) Math.PI / 4F);
            wolfTail.setRotationPoint(-1.0F, 21.0F, 6.0F);
            wolfLeg1.setRotationPoint(-2.5F, 22.0F, 2.0F);
            wolfLeg1.rotateAngleX = ((float) Math.PI * 3F / 2F);
            wolfLeg2.setRotationPoint(0.5F, 22.0F, 2.0F);
            wolfLeg2.rotateAngleX = ((float) Math.PI * 3F / 2F);
            wolfLeg3.rotateAngleX = 5.811947F;
            wolfLeg3.setRotationPoint(-2.49F, 17.0F, -4.0F);
            wolfLeg4.rotateAngleX = 5.811947F;
            wolfLeg4.setRotationPoint(0.51F, 17.0F, -4.0F);
        } else {
            wolfBody.setRotationPoint(0.0F, 14.0F, 2.0F);
            wolfBody.rotateAngleX = ((float) Math.PI / 2F);
            wolfMane.setRotationPoint(-1.0F, 14.0F, -3.0F);
            wolfMane.rotateAngleX = wolfBody.rotateAngleX;
            wolfTail.setRotationPoint(-1.0F, 12.0F, 8.0F);
            wolfLeg1.setRotationPoint(-2.5F, 16.0F, 7.0F);
            wolfLeg2.setRotationPoint(0.5F, 16.0F, 7.0F);
            wolfLeg3.setRotationPoint(-2.5F, 16.0F, -4.0F);
            wolfLeg4.setRotationPoint(0.5F, 16.0F, -4.0F);
            wolfLeg1.rotateAngleX = MathHelper.cos(p_78086_2_ * 0.6662F) * 1.4F * p_78086_3_;
            wolfLeg2.rotateAngleX = MathHelper.cos(p_78086_2_ * 0.6662F + (float) Math.PI) * 1.4F * p_78086_3_;
            wolfLeg3.rotateAngleX = MathHelper.cos(p_78086_2_ * 0.6662F + (float) Math.PI) * 1.4F * p_78086_3_;
            wolfLeg4.rotateAngleX = MathHelper.cos(p_78086_2_ * 0.6662F) * 1.4F * p_78086_3_;
        }

        wolfHeadMain.rotateAngleZ = entitywolf.getInterestedAngle(partialTickTime)
                + entitywolf.getShakeAngle(partialTickTime, 0.0F);
        wolfMane.rotateAngleZ = entitywolf.getShakeAngle(partialTickTime, -0.08F);
        wolfBody.rotateAngleZ = entitywolf.getShakeAngle(partialTickTime, -0.16F);
        wolfTail.rotateAngleZ = entitywolf.getShakeAngle(partialTickTime, -0.2F);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        wolfHeadMain.rotateAngleX = headPitch / (180F / (float) Math.PI);
        wolfHeadMain.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
        wolfTail.rotateAngleX = ageInTicks;
    }
}
