package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.MathHelper;

public class ModelBat extends ModelBase {
    public ModelRenderer batHead;
    public ModelRenderer batBody;
    public ModelRenderer batRightWing;
    public ModelRenderer batLeftWing;
    public ModelRenderer batOuterRightWing;
    public ModelRenderer batOuterLeftWing;

    public ModelBat() {
        textureWidth = 64;
        textureHeight = 64;
        batHead = new ModelRenderer(this, 0, 0);
        batHead.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
        ModelRenderer modelrenderer = new ModelRenderer(this, 24, 0);
        modelrenderer.addBox(-4.0F, -6.0F, -2.0F, 3, 4, 1);
        batHead.addChild(modelrenderer);
        ModelRenderer modelrenderer1 = new ModelRenderer(this, 24, 0);
        modelrenderer1.mirror = true;
        modelrenderer1.addBox(1.0F, -6.0F, -2.0F, 3, 4, 1);
        batHead.addChild(modelrenderer1);
        batBody = new ModelRenderer(this, 0, 16);
        batBody.addBox(-3.0F, 4.0F, -3.0F, 6, 12, 6);
        batBody.setTextureOffset(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10, 6, 1);
        batRightWing = new ModelRenderer(this, 42, 0);
        batRightWing.addBox(-12.0F, 1.0F, 1.5F, 10, 16, 1);
        batOuterRightWing = new ModelRenderer(this, 24, 16);
        batOuterRightWing.setRotationPoint(-12.0F, 1.0F, 1.5F);
        batOuterRightWing.addBox(-8.0F, 1.0F, 0.0F, 8, 12, 1);
        batLeftWing = new ModelRenderer(this, 42, 0);
        batLeftWing.mirror = true;
        batLeftWing.addBox(2.0F, 1.0F, 1.5F, 10, 16, 1);
        batOuterLeftWing = new ModelRenderer(this, 24, 16);
        batOuterLeftWing.mirror = true;
        batOuterLeftWing.setRotationPoint(12.0F, 1.0F, 1.5F);
        batOuterLeftWing.addBox(0.0F, 1.0F, 0.0F, 8, 12, 1);
        batBody.addChild(batRightWing);
        batBody.addChild(batLeftWing);
        batRightWing.addChild(batOuterRightWing);
        batLeftWing.addChild(batOuterLeftWing);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        batHead.render(scale);
        batBody.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        if (((EntityBat) entityIn).getIsBatHanging()) {
            float f = (180F / (float) Math.PI);
            batHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
            batHead.rotateAngleY = (float) Math.PI - netHeadYaw / (180F / (float) Math.PI);
            batHead.rotateAngleZ = (float) Math.PI;
            batHead.setRotationPoint(0.0F, -2.0F, 0.0F);
            batRightWing.setRotationPoint(-3.0F, 0.0F, 3.0F);
            batLeftWing.setRotationPoint(3.0F, 0.0F, 3.0F);
            batBody.rotateAngleX = (float) Math.PI;
            batRightWing.rotateAngleX = -0.15707964F;
            batRightWing.rotateAngleY = -((float) Math.PI * 2F / 5F);
            batOuterRightWing.rotateAngleY = -1.7278761F;
            batLeftWing.rotateAngleX = batRightWing.rotateAngleX;
            batLeftWing.rotateAngleY = -batRightWing.rotateAngleY;
            batOuterLeftWing.rotateAngleY = -batOuterRightWing.rotateAngleY;
        } else {
            float f1 = (180F / (float) Math.PI);
            batHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
            batHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
            batHead.rotateAngleZ = 0.0F;
            batHead.setRotationPoint(0.0F, 0.0F, 0.0F);
            batRightWing.setRotationPoint(0.0F, 0.0F, 0.0F);
            batLeftWing.setRotationPoint(0.0F, 0.0F, 0.0F);
            batBody.rotateAngleX = ((float) Math.PI / 4F) + MathHelper.cos(ageInTicks * 0.1F) * 0.15F;
            batBody.rotateAngleY = 0.0F;
            batRightWing.rotateAngleY = MathHelper.cos(ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
            batLeftWing.rotateAngleY = -batRightWing.rotateAngleY;
            batOuterRightWing.rotateAngleY = batRightWing.rotateAngleY * 0.5F;
            batOuterLeftWing.rotateAngleY = -batRightWing.rotateAngleY * 0.5F;
        }
    }
}
