package net.optifine.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.src.Config;

public class Blender {

    public static int parseBlend(String str) {
        if (str == null) {
            return 1;
        } else {
            str = str.toLowerCase().trim();

            return switch (str) {
                case "alpha" -> 0;
                case "add" -> 1;
                case "subtract" -> 2;
                case "multiply" -> 3;
                case "dodge" -> 4;
                case "burn" -> 5;
                case "screen" -> 6;
                case "overlay" -> 7;
                case "replace" -> 8;
                default -> {
                    Config.warn("Unknown blend: " + str);
                    yield 1;
                }
            };
        }
    }

    public static void setupBlend(int blend, float brightness) {
        switch (blend) {
            case 0:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
                break;

            case 1:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
                break;

            case 2:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(775, 0);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 3:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(774, 771);
                GlStateManager.color(brightness, brightness, brightness, brightness);
                break;

            case 4:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(1, 1);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 5:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(0, 769);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 6:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(1, 769);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 7:
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(774, 768);
                GlStateManager.color(brightness, brightness, brightness, 1.0F);
                break;

            case 8:
                GlStateManager.enableAlpha();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
        }

        GlStateManager.enableTexture2D();
    }

    public static void clearBlend(float rainBrightness) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, rainBrightness);
    }
}
