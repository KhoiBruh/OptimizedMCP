package net.optifine.shaders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class CustomTextureLocation implements ICustomTexture {
    private final int textureUnit;
    private final ResourceLocation location;
    private final int variant;
    private ITextureObject texture;

    public CustomTextureLocation(int textureUnit, ResourceLocation location, int variant) {
        this.textureUnit = textureUnit;
        this.location = location;
        this.variant = variant;
    }

    public ITextureObject getTexture() {
        if (texture == null) {
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            texture = texturemanager.getTexture(location);

            if (texture == null) {
                texture = new SimpleTexture(location);
                texturemanager.loadTexture(location, texture);
                texture = texturemanager.getTexture(location);
            }
        }

        return texture;
    }

    public int getTextureId() {
        ITextureObject itextureobject = getTexture();

        if (variant != 0 && itextureobject instanceof AbstractTexture abstracttexture) {
            MultiTexID multitexid = abstracttexture.multiTex;

            if (multitexid != null) {
                if (variant == 1) {
                    return multitexid.norm;
                }

                if (variant == 2) {
                    return multitexid.spec;
                }
            }
        }

        return itextureobject.getGlTextureId();
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public void deleteTexture() {
    }

    public int getTarget() {
        return 3553;
    }

    public String toString() {
        return "textureUnit: " + textureUnit + ", location: " + location + ", glTextureId: " + (texture != null ? Integer.valueOf(texture.getGlTextureId()) : "");
    }
}
