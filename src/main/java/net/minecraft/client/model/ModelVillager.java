package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelVillager extends ModelBase {
    public ModelRenderer villagerHead;
    public ModelRenderer villagerBody;
    public ModelRenderer villagerArms;
    public ModelRenderer rightVillagerLeg;
    public ModelRenderer leftVillagerLeg;
    public ModelRenderer villagerNose;

    public ModelVillager(float p_i1163_1_) {
        this(p_i1163_1_, 0.0F, 64, 64);
    }

    public ModelVillager(float p_i1164_1_, float p_i1164_2_, int p_i1164_3_, int p_i1164_4_) {
        villagerHead = (new ModelRenderer(this)).setTextureSize(p_i1164_3_, p_i1164_4_);
        villagerHead.setRotationPoint(0.0F, 0.0F + p_i1164_2_, 0.0F);
        villagerHead.setTextureOffset(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, p_i1164_1_);
        villagerNose = (new ModelRenderer(this)).setTextureSize(p_i1164_3_, p_i1164_4_);
        villagerNose.setRotationPoint(0.0F, p_i1164_2_ - 2.0F, 0.0F);
        villagerNose.setTextureOffset(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2, p_i1164_1_);
        villagerHead.addChild(villagerNose);
        villagerBody = (new ModelRenderer(this)).setTextureSize(p_i1164_3_, p_i1164_4_);
        villagerBody.setRotationPoint(0.0F, 0.0F + p_i1164_2_, 0.0F);
        villagerBody.setTextureOffset(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, p_i1164_1_);
        villagerBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, p_i1164_1_ + 0.5F);
        villagerArms = (new ModelRenderer(this)).setTextureSize(p_i1164_3_, p_i1164_4_);
        villagerArms.setRotationPoint(0.0F, 0.0F + p_i1164_2_ + 2.0F, 0.0F);
        villagerArms.setTextureOffset(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, p_i1164_1_);
        villagerArms.setTextureOffset(44, 22).addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, p_i1164_1_);
        villagerArms.setTextureOffset(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, p_i1164_1_);
        rightVillagerLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(p_i1164_3_, p_i1164_4_);
        rightVillagerLeg.setRotationPoint(-2.0F, 12.0F + p_i1164_2_, 0.0F);
        rightVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i1164_1_);
        leftVillagerLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(p_i1164_3_, p_i1164_4_);
        leftVillagerLeg.mirror = true;
        leftVillagerLeg.setRotationPoint(2.0F, 12.0F + p_i1164_2_, 0.0F);
        leftVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i1164_1_);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        villagerHead.render(scale);
        villagerBody.render(scale);
        rightVillagerLeg.render(scale);
        leftVillagerLeg.render(scale);
        villagerArms.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        villagerHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
        villagerHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
        villagerArms.rotationPointY = 3.0F;
        villagerArms.rotationPointZ = -1.0F;
        villagerArms.rotateAngleX = -0.75F;
        rightVillagerLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
        leftVillagerLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * 0.5F;
        rightVillagerLeg.rotateAngleY = 0.0F;
        leftVillagerLeg.rotateAngleY = 0.0F;
    }
}
