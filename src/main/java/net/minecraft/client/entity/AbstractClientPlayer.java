package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.optifine.player.CapeUtils;
import net.optifine.player.PlayerConfigurations;

import java.io.File;

public abstract class AbstractClientPlayer extends EntityPlayer {
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
    private NetworkPlayerInfo playerInfo;
    private ResourceLocation locationOfCape = null;
    private long reloadCapeTimeMs = 0L;
    private boolean elytraOfCape = false;
    private String nameClear = null;

    public AbstractClientPlayer(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
        nameClear = playerProfile.getName();

        if (nameClear != null && !nameClear.isEmpty()) {
            nameClear = StringUtils.stripControlCodes(nameClear);
        }

        CapeUtils.downloadCape(this);
        PlayerConfigurations.getPlayerConfiguration(this);
    }

    public static ThreadDownloadImageData getDownloadImageSkin(ResourceLocation resourceLocationIn, String username) {
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject itextureobject = texturemanager.getTexture(resourceLocationIn);

        if (itextureobject == null) {
            itextureobject = new ThreadDownloadImageData(null,
                    String.format("http://skins.minecraft.net/MinecraftSkins/%s.png",
                            StringUtils.stripControlCodes(username)),
                    DefaultPlayerSkin.getDefaultSkin(getOfflineUUID(username)), new ImageBufferDownload());
            texturemanager.loadTexture(resourceLocationIn, itextureobject);
        }

        return (ThreadDownloadImageData) itextureobject;
    }

    public static ResourceLocation getLocationSkin(String username) {
        return new ResourceLocation("skins/" + StringUtils.stripControlCodes(username));
    }

    public boolean isSpectator() {
        NetworkPlayerInfo networkplayerinfo = Minecraft.getMinecraft().getNetHandler()
                .getPlayerInfo(getGameProfile().getId());
        return networkplayerinfo != null && networkplayerinfo.getGameType() == WorldSettings.GameType.SPECTATOR;
    }

    public boolean hasPlayerInfo() {
        return getPlayerInfo() != null;
    }

    protected NetworkPlayerInfo getPlayerInfo() {
        if (playerInfo == null) {
            playerInfo = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(getUniqueID());
        }

        return playerInfo;
    }

    public boolean hasSkin() {
        NetworkPlayerInfo networkplayerinfo = getPlayerInfo();
        return networkplayerinfo != null && networkplayerinfo.hasLocationSkin();
    }

    public ResourceLocation getLocationSkin() {
        NetworkPlayerInfo networkplayerinfo = getPlayerInfo();
        return networkplayerinfo == null ? DefaultPlayerSkin.getDefaultSkin(getUniqueID())
                : networkplayerinfo.getLocationSkin();
    }

    public ResourceLocation getLocationCape() {
        if (!Config.isShowCapes()) {
            return null;
        } else {
            if (reloadCapeTimeMs != 0L && System.currentTimeMillis() > reloadCapeTimeMs) {
                CapeUtils.reloadCape(this);
                reloadCapeTimeMs = 0L;
            }

            if (locationOfCape != null) {
                return locationOfCape;
            } else {
                NetworkPlayerInfo networkplayerinfo = getPlayerInfo();
                return networkplayerinfo == null ? null : networkplayerinfo.getLocationCape();
            }
        }
    }

    public String getSkinType() {
        NetworkPlayerInfo networkplayerinfo = getPlayerInfo();
        return networkplayerinfo == null ? DefaultPlayerSkin.getSkinType(getUniqueID())
                : networkplayerinfo.getSkinType();
    }

    public float getFovModifier() {
        float f = 1.0F;

        if (capabilities.isFlying) {
            f *= 1.1F;
        }

        IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        f = (float) ((double) f
                * ((iattributeinstance.getAttributeValue() / (double) capabilities.getWalkSpeed() + 1.0D) / 2.0D));

        if (capabilities.getWalkSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
            f = 1.0F;
        }

        if (isUsingItem() && getItemInUse().getItem() == Items.bow) {
            int i = getItemInUseDuration();
            float f1 = (float) i / 20.0F;

            if (f1 > 1.0F) {
                f1 = 1.0F;
            } else {
                f1 = f1 * f1;
            }

            f *= 1.0F - f1 * 0.15F;
        }

        return f;
    }

    public String getNameClear() {
        return nameClear;
    }

    public ResourceLocation getLocationOfCape() {
        return locationOfCape;
    }

    public void setLocationOfCape(ResourceLocation p_setLocationOfCape_1_) {
        locationOfCape = p_setLocationOfCape_1_;
    }

    public boolean hasElytraCape() {
        ResourceLocation resourcelocation = getLocationCape();
        return resourcelocation != null && (resourcelocation != locationOfCape || elytraOfCape);
    }

    public boolean isElytraOfCape() {
        return elytraOfCape;
    }

    public void setElytraOfCape(boolean p_setElytraOfCape_1_) {
        elytraOfCape = p_setElytraOfCape_1_;
    }

    public long getReloadCapeTimeMs() {
        return reloadCapeTimeMs;
    }

    public void setReloadCapeTimeMs(long p_setReloadCapeTimeMs_1_) {
        reloadCapeTimeMs = p_setReloadCapeTimeMs_1_;
    }

    public Vec3 getLook(float partialTicks) {
        return getVectorForRotation(rotationPitch, rotationYaw);
    }
}
