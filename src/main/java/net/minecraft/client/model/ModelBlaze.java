package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBlaze extends ModelBase {
    public ModelRenderer[] blazeSticks = new ModelRenderer[12];
    public ModelRenderer blazeHead;

    public ModelBlaze() {
        for (int i = 0; i < blazeSticks.length; ++i) {
            blazeSticks[i] = new ModelRenderer(this, 0, 16);
            blazeSticks[i].addBox(0.0F, 0.0F, 0.0F, 2, 8, 2);
        }

        blazeHead = new ModelRenderer(this, 0, 0);
        blazeHead.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
    }

    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
                       float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        blazeHead.render(scale);

        for (ModelRenderer blazeStick : blazeSticks) {
            blazeStick.render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        float f = ageInTicks * (float) Math.PI * -0.1F;

        for (int i = 0; i < 4; ++i) {
            blazeSticks[i].rotationPointY = -2.0F + MathHelper.cos(((float) (i * 2) + ageInTicks) * 0.25F);
            blazeSticks[i].rotationPointX = MathHelper.cos(f) * 9.0F;
            blazeSticks[i].rotationPointZ = MathHelper.sin(f) * 9.0F;
            ++f;
        }

        f = ((float) Math.PI / 4F) + ageInTicks * (float) Math.PI * 0.03F;

        for (int j = 4; j < 8; ++j) {
            blazeSticks[j].rotationPointY = 2.0F + MathHelper.cos(((float) (j * 2) + ageInTicks) * 0.25F);
            blazeSticks[j].rotationPointX = MathHelper.cos(f) * 7.0F;
            blazeSticks[j].rotationPointZ = MathHelper.sin(f) * 7.0F;
            ++f;
        }

        f = 0.47123894F + ageInTicks * (float) Math.PI * -0.05F;

        for (int k = 8; k < 12; ++k) {
            blazeSticks[k].rotationPointY = 11.0F + MathHelper.cos(((float) k * 1.5F + ageInTicks) * 0.5F);
            blazeSticks[k].rotationPointX = MathHelper.cos(f) * 5.0F;
            blazeSticks[k].rotationPointZ = MathHelper.sin(f) * 5.0F;
            ++f;
        }

        blazeHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
        blazeHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
    }
}
