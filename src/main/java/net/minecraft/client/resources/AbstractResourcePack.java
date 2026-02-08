package net.minecraft.client.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class AbstractResourcePack implements IResourcePack {
    private static final Logger resourceLog = LogManager.getLogger();
    public final File resourcePackFile;

    public AbstractResourcePack(File resourcePackFileIn) {
        resourcePackFile = resourcePackFileIn;
    }

    private static String locationToName(ResourceLocation location) {
        return String.format("%s/%s/%s", "assets", location.getResourceDomain(), location.getResourcePath());
    }

    protected static String getRelativeName(File p_110595_0_, File p_110595_1_) {
        return p_110595_0_.toURI().relativize(p_110595_1_.toURI()).getPath();
    }

    static <T extends IMetadataSection> T readMetadata(IMetadataSerializer metadataSerializer, InputStream stream, String data) {
        JsonObject jsonobject;

        try (var bufferedreader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            jsonobject = JsonParser.parseReader(bufferedreader).getAsJsonObject();
        } catch (IOException e) {
            throw new JsonParseException(e);
        }

        return metadataSerializer.parseMetadataSection(data, jsonobject);
    }

    public InputStream getInputStream(ResourceLocation location) throws IOException {
        return getInputStreamByName(locationToName(location));
    }

    public boolean resourceExists(ResourceLocation location) {
        return hasResourceName(locationToName(location));
    }

    protected abstract InputStream getInputStreamByName(String name) throws IOException;

    protected abstract boolean hasResourceName(String name);

    protected void logNameNotLowercase(String name) {
        resourceLog.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", new Object[]{name, resourcePackFile});
    }

    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return readMetadata(metadataSerializer, getInputStreamByName("pack.mcmeta"), metadataSectionName);
    }

    public BufferedImage getPackImage() throws IOException {
        return TextureUtil.readBufferedImage(getInputStreamByName("pack.png"));
    }

    public String getPackName() {
        return resourcePackFile.getName();
    }
}
