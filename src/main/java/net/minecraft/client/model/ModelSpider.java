package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelSpider extends ModelBase {
    public ModelRenderer spiderHead;
    public ModelRenderer spiderNeck;
    public ModelRenderer spiderBody;
    public ModelRenderer spiderLeg1;
    public ModelRenderer spiderLeg2;
    public ModelRenderer spiderLeg3;
    public ModelRenderer spiderLeg4;
    public ModelRenderer spiderLeg5;
    public ModelRenderer spiderLeg6;
    public ModelRenderer spiderLeg7;
    public ModelRenderer spiderLeg8;

    public ModelSpider() {
        float f = 0.0F;
        int i = 15;
        spiderHead = new ModelRenderer(this, 32, 4);
        spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, f);
        spiderHead.setRotationPoint(0.0F, (float) i, -3.0F);
        spiderNeck = new ModelRenderer(this, 0, 0);
        spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, f);
        spiderNeck.setRotationPoint(0.0F, (float) i, 0.0F);
        spiderBody = new ModelRenderer(this, 0, 12);
        spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, f);
        spiderBody.setRotationPoint(0.0F, (float) i, 9.0F);
        spiderLeg1 = new ModelRenderer(this, 18, 0);
        spiderLeg1.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg1.setRotationPoint(-4.0F, (float) i, 2.0F);
        spiderLeg2 = new ModelRenderer(this, 18, 0);
        spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg2.setRotationPoint(4.0F, (float) i, 2.0F);
        spiderLeg3 = new ModelRenderer(this, 18, 0);
        spiderLeg3.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg3.setRotationPoint(-4.0F, (float) i, 1.0F);
        spiderLeg4 = new ModelRenderer(this, 18, 0);
        spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg4.setRotationPoint(4.0F, (float) i, 1.0F);
        spiderLeg5 = new ModelRenderer(this, 18, 0);
        spiderLeg5.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg5.setRotationPoint(-4.0F, (float) i, 0.0F);
        spiderLeg6 = new ModelRenderer(this, 18, 0);
        spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg6.setRotationPoint(4.0F, (float) i, 0.0F);
        spiderLeg7 = new ModelRenderer(this, 18, 0);
        spiderLeg7.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg7.setRotationPoint(-4.0F, (float) i, -1.0F);
        spiderLeg8 = new ModelRenderer(this, 18, 0);
        spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, f);
        spiderLeg8.setRotationPoint(4.0F, (float) i, -1.0F);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        spiderHead.render(scale);
        spiderNeck.render(scale);
        spiderBody.render(scale);
        spiderLeg1.render(scale);
        spiderLeg2.render(scale);
        spiderLeg3.render(scale);
        spiderLeg4.render(scale);
        spiderLeg5.render(scale);
        spiderLeg6.render(scale);
        spiderLeg7.render(scale);
        spiderLeg8.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        spiderHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
        spiderHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
        float f = ((float) Math.PI / 4F);
        spiderLeg1.rotateAngleZ = -f;
        spiderLeg2.rotateAngleZ = f;
        spiderLeg3.rotateAngleZ = -f * 0.74F;
        spiderLeg4.rotateAngleZ = f * 0.74F;
        spiderLeg5.rotateAngleZ = -f * 0.74F;
        spiderLeg6.rotateAngleZ = f * 0.74F;
        spiderLeg7.rotateAngleZ = -f;
        spiderLeg8.rotateAngleZ = f;
        float f1 = -0.0F;
        float f2 = 0.3926991F;
        spiderLeg1.rotateAngleY = f2 * 2.0F + f1;
        spiderLeg2.rotateAngleY = -f2 * 2.0F - f1;
        spiderLeg3.rotateAngleY = f2 + f1;
        spiderLeg4.rotateAngleY = -f2 - f1;
        spiderLeg5.rotateAngleY = -f2 + f1;
        spiderLeg6.rotateAngleY = f2 - f1;
        spiderLeg7.rotateAngleY = -f2 * 2.0F + f1;
        spiderLeg8.rotateAngleY = f2 * 2.0F - f1;
        float f3 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
        float f4 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float f5 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float f6 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        float f7 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + 0.0F) * 0.4F) * limbSwingAmount;
        float f8 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float f9 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + ((float) Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float f10 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + ((float) Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        spiderLeg1.rotateAngleY += f3;
        spiderLeg2.rotateAngleY -= f3;
        spiderLeg3.rotateAngleY += f4;
        spiderLeg4.rotateAngleY -= f4;
        spiderLeg5.rotateAngleY += f5;
        spiderLeg6.rotateAngleY -= f5;
        spiderLeg7.rotateAngleY += f6;
        spiderLeg8.rotateAngleY -= f6;
        spiderLeg1.rotateAngleZ += f7;
        spiderLeg2.rotateAngleZ -= f7;
        spiderLeg3.rotateAngleZ += f8;
        spiderLeg4.rotateAngleZ -= f8;
        spiderLeg5.rotateAngleZ += f9;
        spiderLeg6.rotateAngleZ -= f9;
        spiderLeg7.rotateAngleZ += f10;
        spiderLeg8.rotateAngleZ -= f10;
    }
}
