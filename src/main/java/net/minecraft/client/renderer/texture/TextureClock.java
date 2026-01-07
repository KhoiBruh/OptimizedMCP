package net.minecraft.client.renderer.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class TextureClock extends TextureAtlasSprite {
    private double currentAngle;
    private double angleDelta;

    public TextureClock(String iconName) {
        super(iconName);
    }

    public void updateAnimation() {
        if (!framesTextureData.isEmpty()) {
            Minecraft minecraft = Minecraft.getMinecraft();
            double d0 = 0.0D;

            if (minecraft.theWorld != null && minecraft.thePlayer != null) {
                d0 = minecraft.theWorld.getCelestialAngle(1.0F);

                if (!minecraft.theWorld.provider.isSurfaceWorld()) {
                    d0 = Math.random();
                }
            }

            double d1;

            for (d1 = d0 - currentAngle; d1 < -0.5D; ++d1) {
            }

            while (d1 >= 0.5D) {
                --d1;
            }

            d1 = MathHelper.clamp_double(d1, -1.0D, 1.0D);
            angleDelta += d1 * 0.1D;
            angleDelta *= 0.8D;
            currentAngle += angleDelta;
            int i;

            for (i = (int) ((currentAngle + 1.0D) * (double) framesTextureData.size()) % framesTextureData.size(); i < 0; i = (i + framesTextureData.size()) % framesTextureData.size()) {
            }

            if (i != frameCounter) {
                frameCounter = i;
                TextureUtil.uploadTextureMipmap(framesTextureData.get(frameCounter), width, height, originX, originY, false, false);
            }
        }
    }
}
