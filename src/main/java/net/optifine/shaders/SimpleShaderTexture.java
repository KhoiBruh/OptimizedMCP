package net.optifine.shaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class SimpleShaderTexture extends AbstractTexture {
    private static final IMetadataSerializer METADATA_SERIALIZER = makeMetadataSerializer();
    private final String texturePath;

    public SimpleShaderTexture(String texturePath) {
        this.texturePath = texturePath;
    }

    public static TextureMetadataSection loadTextureMetadataSection(String texturePath, TextureMetadataSection def) {
        String s = texturePath + ".mcmeta";
        String s1 = "texture";

        try (var inputstream = Shaders.getShaderPackResourceStream(s)) {
            if (inputstream != null) {
                try (var reader = new InputStreamReader(inputstream)) {
                    TextureMetadataSection metadataSection;

                    try {
                        JsonObject jsonobject = JsonParser.parseReader(reader).getAsJsonObject();
                        TextureMetadataSection texturemetadatasection = METADATA_SERIALIZER.parseMetadataSection(s1, jsonobject);

                        if (texturemetadatasection == null) return def;

                        metadataSection = texturemetadatasection;
                    } catch (RuntimeException runtimeexception) {
                        SMCLog.warning("Error reading metadata: " + s);
                        SMCLog.warning(runtimeexception.getClass().getName() + ": " + runtimeexception.getMessage());
                        return def;
                    }

                    return metadataSection;
                }
            } else return def;
        } catch (IOException ioexception) {
            return def;
        }
    }

    private static IMetadataSerializer makeMetadataSerializer() {
        IMetadataSerializer imetadataserializer = new IMetadataSerializer();
        imetadataserializer.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
        return imetadataserializer;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        deleteGlTexture();

        try (var inputstream = Shaders.getShaderPackResourceStream(texturePath)) {
            if (inputstream != null) {
                BufferedImage bufferedimage = TextureUtil.readBufferedImage(inputstream);
                TextureMetadataSection texturemetadatasection = loadTextureMetadataSection(texturePath, new TextureMetadataSection(false, false, new ArrayList<>()));
                TextureUtil.uploadTextureImageAllocate(getGlTextureId(), bufferedimage, texturemetadatasection.textureBlur(), texturemetadatasection.textureClamp());
            } else throw new FileNotFoundException("Shader texture not found: " + texturePath);
        }
    }
}
