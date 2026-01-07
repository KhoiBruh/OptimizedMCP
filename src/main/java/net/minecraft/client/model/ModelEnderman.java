package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelEnderman extends ModelBiped {
    public boolean isCarrying;
    public boolean isAttacking;

    public ModelEnderman(float p_i46305_1_) {
        super(0.0F, -14.0F, 64, 32);
        float f = -14.0F;
        bipedHeadwear = new ModelRenderer(this, 0, 16);
        bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46305_1_ - 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F + f, 0.0F);
        bipedBody = new ModelRenderer(this, 32, 16);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, p_i46305_1_);
        bipedBody.setRotationPoint(0.0F, 0.0F + f, 0.0F);
        bipedRightArm = new ModelRenderer(this, 56, 0);
        bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedRightArm.setRotationPoint(-3.0F, 2.0F + f, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 56, 0);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + f, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 56, 0);
        bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F + f, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 56, 0);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F + f, 0.0F);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        bipedHead.showModel = true;
        float f = -14.0F;
        bipedBody.rotateAngleX = 0.0F;
        bipedBody.rotationPointY = f;
        bipedBody.rotationPointZ = -0.0F;
        bipedRightLeg.rotateAngleX -= 0.0F;
        bipedLeftLeg.rotateAngleX -= 0.0F;
        bipedRightArm.rotateAngleX = (float) ((double) bipedRightArm.rotateAngleX * 0.5D);
        bipedLeftArm.rotateAngleX = (float) ((double) bipedLeftArm.rotateAngleX * 0.5D);
        bipedRightLeg.rotateAngleX = (float) ((double) bipedRightLeg.rotateAngleX * 0.5D);
        bipedLeftLeg.rotateAngleX = (float) ((double) bipedLeftLeg.rotateAngleX * 0.5D);
        float f1 = 0.4F;

        if (bipedRightArm.rotateAngleX > f1) {
            bipedRightArm.rotateAngleX = f1;
        }

        if (bipedLeftArm.rotateAngleX > f1) {
            bipedLeftArm.rotateAngleX = f1;
        }

        if (bipedRightArm.rotateAngleX < -f1) {
            bipedRightArm.rotateAngleX = -f1;
        }

        if (bipedLeftArm.rotateAngleX < -f1) {
            bipedLeftArm.rotateAngleX = -f1;
        }

        if (bipedRightLeg.rotateAngleX > f1) {
            bipedRightLeg.rotateAngleX = f1;
        }

        if (bipedLeftLeg.rotateAngleX > f1) {
            bipedLeftLeg.rotateAngleX = f1;
        }

        if (bipedRightLeg.rotateAngleX < -f1) {
            bipedRightLeg.rotateAngleX = -f1;
        }

        if (bipedLeftLeg.rotateAngleX < -f1) {
            bipedLeftLeg.rotateAngleX = -f1;
        }

        if (isCarrying) {
            bipedRightArm.rotateAngleX = -0.5F;
            bipedLeftArm.rotateAngleX = -0.5F;
            bipedRightArm.rotateAngleZ = 0.05F;
            bipedLeftArm.rotateAngleZ = -0.05F;
        }

        bipedRightArm.rotationPointZ = 0.0F;
        bipedLeftArm.rotationPointZ = 0.0F;
        bipedRightLeg.rotationPointZ = 0.0F;
        bipedLeftLeg.rotationPointZ = 0.0F;
        bipedRightLeg.rotationPointY = 9.0F + f;
        bipedLeftLeg.rotationPointY = 9.0F + f;
        bipedHead.rotationPointZ = -0.0F;
        bipedHead.rotationPointY = f + 1.0F;
        bipedHeadwear.rotationPointX = bipedHead.rotationPointX;
        bipedHeadwear.rotationPointY = bipedHead.rotationPointY;
        bipedHeadwear.rotationPointZ = bipedHead.rotationPointZ;
        bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;
        bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleZ = bipedHead.rotateAngleZ;

        if (isAttacking) {
            float f2 = 1.0F;
            bipedHead.rotationPointY -= f2 * 5.0F;
        }
    }
}
