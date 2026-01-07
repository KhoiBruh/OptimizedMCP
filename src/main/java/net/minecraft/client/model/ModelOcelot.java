package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.MathHelper;

public class ModelOcelot extends ModelBase {
    public ModelRenderer ocelotBackLeftLeg;
    public ModelRenderer ocelotBackRightLeg;
    public ModelRenderer ocelotFrontLeftLeg;
    public ModelRenderer ocelotFrontRightLeg;
    public ModelRenderer ocelotTail;
    public ModelRenderer ocelotTail2;
    public ModelRenderer ocelotHead;
    public ModelRenderer ocelotBody;
    int field_78163_i = 1;

    public ModelOcelot() {
        setTextureOffset("head.main", 0, 0);
        setTextureOffset("head.nose", 0, 24);
        setTextureOffset("head.ear1", 0, 10);
        setTextureOffset("head.ear2", 6, 10);
        ocelotHead = new ModelRenderer(this, "head");
        ocelotHead.addBox("main", -2.5F, -2.0F, -3.0F, 5, 4, 5);
        ocelotHead.addBox("nose", -1.5F, 0.0F, -4.0F, 3, 2, 2);
        ocelotHead.addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2);
        ocelotHead.addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2);
        ocelotHead.setRotationPoint(0.0F, 15.0F, -9.0F);
        ocelotBody = new ModelRenderer(this, 20, 0);
        ocelotBody.addBox(-2.0F, 3.0F, -8.0F, 4, 16, 6, 0.0F);
        ocelotBody.setRotationPoint(0.0F, 12.0F, -10.0F);
        ocelotTail = new ModelRenderer(this, 0, 15);
        ocelotTail.addBox(-0.5F, 0.0F, 0.0F, 1, 8, 1);
        ocelotTail.rotateAngleX = 0.9F;
        ocelotTail.setRotationPoint(0.0F, 15.0F, 8.0F);
        ocelotTail2 = new ModelRenderer(this, 4, 15);
        ocelotTail2.addBox(-0.5F, 0.0F, 0.0F, 1, 8, 1);
        ocelotTail2.setRotationPoint(0.0F, 20.0F, 14.0F);
        ocelotBackLeftLeg = new ModelRenderer(this, 8, 13);
        ocelotBackLeftLeg.addBox(-1.0F, 0.0F, 1.0F, 2, 6, 2);
        ocelotBackLeftLeg.setRotationPoint(1.1F, 18.0F, 5.0F);
        ocelotBackRightLeg = new ModelRenderer(this, 8, 13);
        ocelotBackRightLeg.addBox(-1.0F, 0.0F, 1.0F, 2, 6, 2);
        ocelotBackRightLeg.setRotationPoint(-1.1F, 18.0F, 5.0F);
        ocelotFrontLeftLeg = new ModelRenderer(this, 40, 0);
        ocelotFrontLeftLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 2);
        ocelotFrontLeftLeg.setRotationPoint(1.2F, 13.8F, -5.0F);
        ocelotFrontRightLeg = new ModelRenderer(this, 40, 0);
        ocelotFrontRightLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 2);
        ocelotFrontRightLeg.setRotationPoint(-1.2F, 13.8F, -5.0F);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);

        if (isChild) {
            float f = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.5F / f, 1.5F / f, 1.5F / f);
            GlStateManager.translate(0.0F, 10.0F * scale, 4.0F * scale);
            ocelotHead.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            ocelotBody.render(scale);
            ocelotBackLeftLeg.render(scale);
            ocelotBackRightLeg.render(scale);
            ocelotFrontLeftLeg.render(scale);
            ocelotFrontRightLeg.render(scale);
            ocelotTail.render(scale);
            ocelotTail2.render(scale);
            GlStateManager.popMatrix();
        } else {
            ocelotHead.render(scale);
            ocelotBody.render(scale);
            ocelotTail.render(scale);
            ocelotTail2.render(scale);
            ocelotBackLeftLeg.render(scale);
            ocelotBackRightLeg.render(scale);
            ocelotFrontLeftLeg.render(scale);
            ocelotFrontRightLeg.render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        ocelotHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
        ocelotHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);

        if (field_78163_i != 3) {
            ocelotBody.rotateAngleX = ((float) Math.PI / 2F);

            if (field_78163_i == 2) {
                ocelotBackLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
                ocelotBackRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 0.3F) * 1.0F
                        * limbSwingAmount;
                ocelotFrontLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI + 0.3F)
                        * 1.0F * limbSwingAmount;
                ocelotFrontRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F
                        * limbSwingAmount;
                ocelotTail2.rotateAngleX = 1.7278761F
                        + ((float) Math.PI / 10F) * MathHelper.cos(limbSwing) * limbSwingAmount;
            } else {
                ocelotBackLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
                ocelotBackRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F
                        * limbSwingAmount;
                ocelotFrontLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F
                        * limbSwingAmount;
                ocelotFrontRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;

                if (field_78163_i == 1) {
                    ocelotTail2.rotateAngleX = 1.7278761F
                            + ((float) Math.PI / 4F) * MathHelper.cos(limbSwing) * limbSwingAmount;
                } else {
                    ocelotTail2.rotateAngleX = 1.7278761F
                            + 0.47123894F * MathHelper.cos(limbSwing) * limbSwingAmount;
                }
            }
        }
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_,
                                    float partialTickTime) {
        EntityOcelot entityocelot = (EntityOcelot) entitylivingbaseIn;
        ocelotBody.rotationPointY = 12.0F;
        ocelotBody.rotationPointZ = -10.0F;
        ocelotHead.rotationPointY = 15.0F;
        ocelotHead.rotationPointZ = -9.0F;
        ocelotTail.rotationPointY = 15.0F;
        ocelotTail.rotationPointZ = 8.0F;
        ocelotTail2.rotationPointY = 20.0F;
        ocelotTail2.rotationPointZ = 14.0F;
        ocelotFrontLeftLeg.rotationPointY = ocelotFrontRightLeg.rotationPointY = 13.8F;
        ocelotFrontLeftLeg.rotationPointZ = ocelotFrontRightLeg.rotationPointZ = -5.0F;
        ocelotBackLeftLeg.rotationPointY = ocelotBackRightLeg.rotationPointY = 18.0F;
        ocelotBackLeftLeg.rotationPointZ = ocelotBackRightLeg.rotationPointZ = 5.0F;
        ocelotTail.rotateAngleX = 0.9F;

        if (entityocelot.isSneaking()) {
            ++ocelotBody.rotationPointY;
            ocelotHead.rotationPointY += 2.0F;
            ++ocelotTail.rotationPointY;
            ocelotTail2.rotationPointY += -4.0F;
            ocelotTail2.rotationPointZ += 2.0F;
            ocelotTail.rotateAngleX = ((float) Math.PI / 2F);
            ocelotTail2.rotateAngleX = ((float) Math.PI / 2F);
            field_78163_i = 0;
        } else if (entityocelot.isSprinting()) {
            ocelotTail2.rotationPointY = ocelotTail.rotationPointY;
            ocelotTail2.rotationPointZ += 2.0F;
            ocelotTail.rotateAngleX = ((float) Math.PI / 2F);
            ocelotTail2.rotateAngleX = ((float) Math.PI / 2F);
            field_78163_i = 2;
        } else if (entityocelot.isSitting()) {
            ocelotBody.rotateAngleX = ((float) Math.PI / 4F);
            ocelotBody.rotationPointY += -4.0F;
            ocelotBody.rotationPointZ += 5.0F;
            ocelotHead.rotationPointY += -3.3F;
            ++ocelotHead.rotationPointZ;
            ocelotTail.rotationPointY += 8.0F;
            ocelotTail.rotationPointZ += -2.0F;
            ocelotTail2.rotationPointY += 2.0F;
            ocelotTail2.rotationPointZ += -0.8F;
            ocelotTail.rotateAngleX = 1.7278761F;
            ocelotTail2.rotateAngleX = 2.670354F;
            ocelotFrontLeftLeg.rotateAngleX = ocelotFrontRightLeg.rotateAngleX = -0.15707964F;
            ocelotFrontLeftLeg.rotationPointY = ocelotFrontRightLeg.rotationPointY = 15.8F;
            ocelotFrontLeftLeg.rotationPointZ = ocelotFrontRightLeg.rotationPointZ = -7.0F;
            ocelotBackLeftLeg.rotateAngleX = ocelotBackRightLeg.rotateAngleX = -((float) Math.PI / 2F);
            ocelotBackLeftLeg.rotationPointY = ocelotBackRightLeg.rotationPointY = 21.0F;
            ocelotBackLeftLeg.rotationPointZ = ocelotBackRightLeg.rotationPointZ = 1.0F;
            field_78163_i = 3;
        } else {
            field_78163_i = 1;
        }
    }
}
