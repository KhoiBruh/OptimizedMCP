package net.optifine.player;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class CapeImageBuffer extends ImageBufferDownload {
    private AbstractClientPlayer player;
    private final ResourceLocation resourceLocation;
    private boolean elytraOfCape;

    public CapeImageBuffer(AbstractClientPlayer player, ResourceLocation resourceLocation) {
        this.player = player;
        this.resourceLocation = resourceLocation;
    }

    public BufferedImage parseUserSkin(BufferedImage imageRaw) {
        BufferedImage bufferedimage = CapeUtils.parseCape(imageRaw);
        elytraOfCape = CapeUtils.isElytraCape(imageRaw, bufferedimage);
        return bufferedimage;
    }

    public void skinAvailable() {
        if (player != null) {
            player.setLocationOfCape(resourceLocation);
            player.setElytraOfCape(elytraOfCape);
        }

        cleanup();
    }

    public void cleanup() {
        player = null;
    }

    public boolean isElytraOfCape() {
        return elytraOfCape;
    }
}
