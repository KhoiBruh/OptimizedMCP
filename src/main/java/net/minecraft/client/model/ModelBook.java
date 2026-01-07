package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBook extends ModelBase {
    public ModelRenderer coverRight = (new ModelRenderer(this)).setTextureOffset(0, 0).addBox(-6.0F, -5.0F, 0.0F, 6, 10, 0);
    public ModelRenderer coverLeft = (new ModelRenderer(this)).setTextureOffset(16, 0).addBox(0.0F, -5.0F, 0.0F, 6, 10, 0);
    public ModelRenderer pagesRight = (new ModelRenderer(this)).setTextureOffset(0, 10).addBox(0.0F, -4.0F, -0.99F, 5, 8, 1);
    public ModelRenderer pagesLeft = (new ModelRenderer(this)).setTextureOffset(12, 10).addBox(0.0F, -4.0F, -0.01F, 5, 8, 1);
    public ModelRenderer flippingPageRight = (new ModelRenderer(this)).setTextureOffset(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
    public ModelRenderer flippingPageLeft = (new ModelRenderer(this)).setTextureOffset(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
    public ModelRenderer bookSpine = (new ModelRenderer(this)).setTextureOffset(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2, 10, 0);

    public ModelBook() {
        coverRight.setRotationPoint(0.0F, 0.0F, -1.0F);
        coverLeft.setRotationPoint(0.0F, 0.0F, 1.0F);
        bookSpine.rotateAngleY = ((float) Math.PI / 2F);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        coverRight.render(scale);
        coverLeft.render(scale);
        bookSpine.render(scale);
        pagesRight.render(scale);
        pagesLeft.render(scale);
        flippingPageRight.render(scale);
        flippingPageLeft.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float f = (MathHelper.sin(limbSwing * 0.02F) * 0.1F + 1.25F) * netHeadYaw;
        coverRight.rotateAngleY = (float) Math.PI + f;
        coverLeft.rotateAngleY = -f;
        pagesRight.rotateAngleY = f;
        pagesLeft.rotateAngleY = -f;
        flippingPageRight.rotateAngleY = f - f * 2.0F * limbSwingAmount;
        flippingPageLeft.rotateAngleY = f - f * 2.0F * ageInTicks;
        pagesRight.rotationPointX = MathHelper.sin(f);
        pagesLeft.rotationPointX = MathHelper.sin(f);
        flippingPageRight.rotationPointX = MathHelper.sin(f);
        flippingPageLeft.rotationPointX = MathHelper.sin(f);
    }
}
