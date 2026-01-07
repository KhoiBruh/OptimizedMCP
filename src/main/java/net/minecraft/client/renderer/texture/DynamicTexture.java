package net.minecraft.client.renderer.texture;

import net.minecraft.client.resources.IResourceManager;

import java.awt.image.BufferedImage;

public class DynamicTexture extends AbstractTexture {
    private final int[] dynamicTextureData;
    private final int width;
    private final int height;

    public DynamicTexture(BufferedImage bufferedImage) {
        this(bufferedImage.getWidth(), bufferedImage.getHeight());
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), dynamicTextureData, 0, bufferedImage.getWidth());
        updateDynamicTexture();
    }

    public DynamicTexture(int textureWidth, int textureHeight) {
        width = textureWidth;
        height = textureHeight;
        dynamicTextureData = new int[textureWidth * textureHeight];
        TextureUtil.allocateTexture(getGlTextureId(), textureWidth, textureHeight);
    }

    public void loadTexture(IResourceManager resourceManager) {
    }

    public void updateDynamicTexture() {
        TextureUtil.uploadTexture(getGlTextureId(), dynamicTextureData, width, height);
    }

    public int[] getTextureData() {
        return dynamicTextureData;
    }
}
