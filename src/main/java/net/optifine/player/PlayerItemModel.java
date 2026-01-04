package net.optifine.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerItemModel {
    private boolean usePlayerTexture = false;
    private PlayerItemRenderer[] modelRenderers = new PlayerItemRenderer[0];
    private ResourceLocation textureLocation = null;
    private BufferedImage textureImage = null;
    private DynamicTexture texture = null;
    private final ResourceLocation locationMissing = new ResourceLocation("textures/blocks/wool_colored_red.png");

    public PlayerItemModel(Dimension textureSize, boolean usePlayerTexture, PlayerItemRenderer[] modelRenderers) {
        this.usePlayerTexture = usePlayerTexture;
        this.modelRenderers = modelRenderers;
    }

    public static ModelRenderer getAttachModel(ModelBiped modelBiped, int attachTo) {
        return switch (attachTo) {
            case 0 -> modelBiped.bipedBody;
            case 1 -> modelBiped.bipedHead;
            case 2 -> modelBiped.bipedLeftArm;
            case 3 -> modelBiped.bipedRightArm;
            case 4 -> modelBiped.bipedLeftLeg;
            case 5 -> modelBiped.bipedRightLeg;
            default -> null;
        };
    }

    public void render(ModelBiped modelBiped, AbstractClientPlayer player, float scale, float partialTicks) {
        TextureManager texturemanager = Config.getTextureManager();

        if (this.usePlayerTexture) {
            texturemanager.bindTexture(player.getLocationSkin());
        } else if (this.textureLocation != null) {
            if (this.texture == null && this.textureImage != null) {
                this.texture = new DynamicTexture(this.textureImage);
                Minecraft.getMinecraft().getTextureManager().loadTexture(this.textureLocation, this.texture);
            }

            texturemanager.bindTexture(this.textureLocation);
        } else {
            texturemanager.bindTexture(this.locationMissing);
        }

        for (PlayerItemRenderer playeritemrenderer : this.modelRenderers) {
            GlStateManager.pushMatrix();

            if (player.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            playeritemrenderer.render(modelBiped, scale);
            GlStateManager.popMatrix();
        }
    }

    public void setTextureImage(BufferedImage textureImage) {
        this.textureImage = textureImage;
    }

    public void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    public boolean isUsePlayerTexture() {
        return this.usePlayerTexture;
    }
}
