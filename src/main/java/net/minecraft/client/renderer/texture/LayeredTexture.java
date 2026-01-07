package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class LayeredTexture extends AbstractTexture {
    private static final Logger logger = LogManager.getLogger();
    public final List<String> layeredTextureNames;
    private ResourceLocation textureLocation;

    public LayeredTexture(String... textureNames) {
        layeredTextureNames = Lists.newArrayList(textureNames);

        if (textureNames.length > 0 && textureNames[0] != null) {
            textureLocation = new ResourceLocation(textureNames[0]);
        }
    }

    public void loadTexture(IResourceManager resourceManager) {
        deleteGlTexture();
        BufferedImage bufferedimage = null;

        try {
            for (String s : layeredTextureNames) {
                if (s != null) {
                    InputStream inputstream = resourceManager.getResource(new ResourceLocation(s)).getInputStream();
                    BufferedImage bufferedimage1 = TextureUtil.readBufferedImage(inputstream);

                    if (bufferedimage == null) {
                        bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), 2);
                    }

                    bufferedimage.getGraphics().drawImage(bufferedimage1, 0, 0, null);
                }
            }
        } catch (IOException ioexception) {
            logger.error("Couldn't load layered image", ioexception);
            return;
        }

        if (Config.isShaders()) {
            ShadersTex.loadSimpleTexture(getGlTextureId(), bufferedimage, false, false, resourceManager, textureLocation, getMultiTexID());
        } else {
            TextureUtil.uploadTextureImage(getGlTextureId(), bufferedimage);
        }
    }
}
