package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.MathHelper;

public class ModelWither extends ModelBase {
    public ModelRenderer[] field_82905_a;
    public ModelRenderer[] field_82904_b;

    public ModelWither(float p_i46302_1_) {
        textureWidth = 64;
        textureHeight = 64;
        field_82905_a = new ModelRenderer[3];
        field_82905_a[0] = new ModelRenderer(this, 0, 16);
        field_82905_a[0].addBox(-10.0F, 3.9F, -0.5F, 20, 3, 3, p_i46302_1_);
        field_82905_a[1] = (new ModelRenderer(this)).setTextureSize(textureWidth, textureHeight);
        field_82905_a[1].setRotationPoint(-2.0F, 6.9F, -0.5F);
        field_82905_a[1].setTextureOffset(0, 22).addBox(0.0F, 0.0F, 0.0F, 3, 10, 3, p_i46302_1_);
        field_82905_a[1].setTextureOffset(24, 22).addBox(-4.0F, 1.5F, 0.5F, 11, 2, 2, p_i46302_1_);
        field_82905_a[1].setTextureOffset(24, 22).addBox(-4.0F, 4.0F, 0.5F, 11, 2, 2, p_i46302_1_);
        field_82905_a[1].setTextureOffset(24, 22).addBox(-4.0F, 6.5F, 0.5F, 11, 2, 2, p_i46302_1_);
        field_82905_a[2] = new ModelRenderer(this, 12, 22);
        field_82905_a[2].addBox(0.0F, 0.0F, 0.0F, 3, 6, 3, p_i46302_1_);
        field_82904_b = new ModelRenderer[3];
        field_82904_b[0] = new ModelRenderer(this, 0, 0);
        field_82904_b[0].addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, p_i46302_1_);
        field_82904_b[1] = new ModelRenderer(this, 32, 0);
        field_82904_b[1].addBox(-4.0F, -4.0F, -4.0F, 6, 6, 6, p_i46302_1_);
        field_82904_b[1].rotationPointX = -8.0F;
        field_82904_b[1].rotationPointY = 4.0F;
        field_82904_b[2] = new ModelRenderer(this, 32, 0);
        field_82904_b[2].addBox(-4.0F, -4.0F, -4.0F, 6, 6, 6, p_i46302_1_);
        field_82904_b[2].rotationPointX = 10.0F;
        field_82904_b[2].rotationPointY = 4.0F;
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);

        for (ModelRenderer modelrenderer : field_82904_b) {
            modelrenderer.render(scale);
        }

        for (ModelRenderer modelrenderer1 : field_82905_a) {
            modelrenderer1.render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        float f = MathHelper.cos(ageInTicks * 0.1F);
        field_82905_a[1].rotateAngleX = (0.065F + 0.05F * f) * (float) Math.PI;
        field_82905_a[2].setRotationPoint(-2.0F, 6.9F + MathHelper.cos(field_82905_a[1].rotateAngleX) * 10.0F,
                -0.5F + MathHelper.sin(field_82905_a[1].rotateAngleX) * 10.0F);
        field_82905_a[2].rotateAngleX = (0.265F + 0.1F * f) * (float) Math.PI;
        field_82904_b[0].rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
        field_82904_b[0].rotateAngleX = headPitch / (180F / (float) Math.PI);
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_,
                                    float partialTickTime) {
        EntityWither entitywither = (EntityWither) entitylivingbaseIn;

        for (int i = 1; i < 3; ++i) {
            field_82904_b[i].rotateAngleY = (entitywither.func_82207_a(i - 1) - entitylivingbaseIn.renderYawOffset)
                    / (180F / (float) Math.PI);
            field_82904_b[i].rotateAngleX = entitywither.func_82210_r(i - 1) / (180F / (float) Math.PI);
        }
    }
}
