package net.minecraft.client.renderer.texture;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.EmissiveTextures;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SimpleTexture extends AbstractTexture {
    private static final Logger logger = LogManager.getLogger();
    protected final ResourceLocation textureLocation;
    public ResourceLocation locationEmissive;
    public boolean isEmissive;

    public SimpleTexture(ResourceLocation textureResourceLocation) {
        textureLocation = textureResourceLocation;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        deleteGlTexture();
        InputStream inputstream = null;

        try {
            IResource iresource = resourceManager.getResource(textureLocation);
            inputstream = iresource.getInputStream();
            BufferedImage bufferedimage = TextureUtil.readBufferedImage(inputstream);
            boolean flag = false;
            boolean flag1 = false;

            if (iresource.hasMetadata()) {
                try {
                    TextureMetadataSection texturemetadatasection = iresource.getMetadata("texture");

                    if (texturemetadatasection != null) {
                        flag = texturemetadatasection.textureBlur();
                        flag1 = texturemetadatasection.textureClamp();
                    }
                } catch (RuntimeException runtimeexception) {
                    logger.warn("Failed reading metadata of: {}", textureLocation, runtimeexception);
                }
            }

            if (Config.isShaders()) {
                ShadersTex.loadSimpleTexture(getGlTextureId(), bufferedimage, flag, flag1, resourceManager, textureLocation, getMultiTexID());
            } else {
                TextureUtil.uploadTextureImageAllocate(getGlTextureId(), bufferedimage, flag, flag1);
            }

            if (EmissiveTextures.isActive()) {
                EmissiveTextures.loadTexture(textureLocation, this);
            }
        } finally {
            if (inputstream != null) {
                inputstream.close();
            }
        }
    }
}
