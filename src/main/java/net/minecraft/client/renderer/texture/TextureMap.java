package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.optifine.*;
import net.optifine.shaders.ShadersTex;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class TextureMap extends AbstractTexture implements ITickableTextureObject {
    public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
    public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
    private static final boolean ENABLE_SKIP = Boolean
            .parseBoolean(System.getProperty("fml.skipFirstTextureLoad", "true"));
    private static final Logger logger = LogManager.getLogger();
    private final List<TextureAtlasSprite> listAnimatedSprites;
    private final Map<String, TextureAtlasSprite> mapRegisteredSprites;
    private final Map<String, TextureAtlasSprite> mapUploadedSprites;
    private final String basePath;
    private final IIconCreator iconCreator;
    private final TextureAtlasSprite missingImage;
    public int atlasWidth;
    public int atlasHeight;
    private int mipmapLevels;
    private boolean skipFirst;
    private TextureAtlasSprite[] iconGrid;
    private int iconGridSize;
    private int iconGridCountX;
    private int iconGridCountY;
    private double iconGridSizeU;
    private double iconGridSizeV;
    private final CounterInt counterIndexInMap;
    private int countAnimationsActive;
    private int frameCountAnimations;

    public TextureMap(String p_i46099_1_) {
        this(p_i46099_1_, null);
    }

    public TextureMap(String p_i5_1_, boolean p_i5_2_) {
        this(p_i5_1_, null, p_i5_2_);
    }

    public TextureMap(String p_i46100_1_, IIconCreator iconCreatorIn) {
        this(p_i46100_1_, iconCreatorIn, false);
    }

    public TextureMap(String p_i6_1_, IIconCreator p_i6_2_, boolean p_i6_3_) {
        skipFirst = false;
        iconGrid = null;
        iconGridSize = -1;
        iconGridCountX = -1;
        iconGridCountY = -1;
        iconGridSizeU = -1.0D;
        iconGridSizeV = -1.0D;
        counterIndexInMap = new CounterInt(0);
        atlasWidth = 0;
        atlasHeight = 0;
        listAnimatedSprites = Lists.newArrayList();
        mapRegisteredSprites = Maps.newHashMap();
        mapUploadedSprites = Maps.newHashMap();
        missingImage = new TextureAtlasSprite("missingno");
        basePath = p_i6_1_;
        iconCreator = p_i6_2_;
        skipFirst = p_i6_3_ && ENABLE_SKIP;
    }

    private void initMissingImage() {
        int i = getMinSpriteSize();
        int[] aint = getMissingImageData(i);
        missingImage.setIconWidth(i);
        missingImage.setIconHeight(i);
        int[][] aint1 = new int[mipmapLevels + 1][];
        aint1[0] = aint;
        missingImage.setFramesTextureData(Lists.newArrayList(new int[][][]{aint1}));
        missingImage.setIndexInMap(counterIndexInMap.nextValue());
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        if (iconCreator != null) {
            loadSprites(resourceManager, iconCreator);
        }
    }

    public void loadSprites(IResourceManager resourceManager, IIconCreator p_174943_2_) {
        mapRegisteredSprites.clear();
        counterIndexInMap.reset();
        p_174943_2_.registerSprites(this);

        if (mipmapLevels >= 4) {
            mipmapLevels = detectMaxMipmapLevel(mapRegisteredSprites, resourceManager);
            Config.log("Mipmap levels: " + mipmapLevels);
        }

        initMissingImage();
        deleteGlTexture();
        loadTextureAtlas(resourceManager);
    }

    public void loadTextureAtlas(IResourceManager resourceManager) {
        Config.dbg("Multitexture: " + Config.isMultiTexture());

        if (Config.isMultiTexture()) {
            for (TextureAtlasSprite textureatlassprite : mapUploadedSprites.values()) {
                textureatlassprite.deleteSpriteTexture();
            }
        }

        ConnectedTextures.updateIcons(this);
        CustomItems.updateIcons(this);
        BetterGrass.updateIcons(this);
        int i2 = TextureUtils.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(i2, i2, true, 0, mipmapLevels);
        mapUploadedSprites.clear();
        listAnimatedSprites.clear();
        int i = Integer.MAX_VALUE;

        int j = getMinSpriteSize();
        iconGridSize = j;
        int k = 1 << mipmapLevels;
        int l = 0;
        int i1 = 0;
        Iterator iterator = mapRegisteredSprites.entrySet().iterator();

        while (true) {
            if (iterator.hasNext()) {
                Entry<String, TextureAtlasSprite> entry = (Entry) iterator.next();

                if (!skipFirst) {
                    TextureAtlasSprite textureatlassprite3 = entry.getValue();
                    ResourceLocation resourcelocation1 = new ResourceLocation(textureatlassprite3.getIconName());
                    ResourceLocation resourcelocation2 = completeResourceLocation(resourcelocation1, 0);
                    textureatlassprite3.updateIndexInMap(counterIndexInMap);

                    if (textureatlassprite3.hasCustomLoader(resourceManager, resourcelocation1)) {
                        if (!textureatlassprite3.load(resourceManager, resourcelocation1)) {
                            i = Math.min(i,
                                    Math.min(textureatlassprite3.getIconWidth(), textureatlassprite3.getIconHeight()));
                            stitcher.addSprite(textureatlassprite3);
                            Config.detail("Custom loader (skipped): " + textureatlassprite3);
                            ++i1;
                        }

                        Config.detail("Custom loader: " + textureatlassprite3);
                        ++l;
                        continue;
                    }

                    try {
                        IResource iresource = resourceManager.getResource(resourcelocation2);
                        BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];
                        abufferedimage[0] = TextureUtil.readBufferedImage(iresource.getInputStream());
                        int k3 = abufferedimage[0].getWidth();
                        int l3 = abufferedimage[0].getHeight();

                        if (k3 < 1 || l3 < 1) {
                            Config.warn("Invalid sprite size: " + textureatlassprite3);
                            continue;
                        }

                        if (k3 < j || mipmapLevels > 0) {
                            int i4 = mipmapLevels > 0 ? TextureUtils.scaleToGrid(k3, j)
                                    : TextureUtils.scaleToMin(k3, j);

                            if (i4 != k3) {
                                if (!TextureUtils.isPowerOfTwo(k3)) {
                                    Config.log("Scaled non power of 2: " + textureatlassprite3.getIconName() + ", " + k3
                                            + " -> " + i4);
                                } else {
                                    Config.log("Scaled too small texture: " + textureatlassprite3.getIconName() + ", "
                                            + k3 + " -> " + i4);
                                }

                                int j1 = l3 * i4 / k3;
                                abufferedimage[0] = TextureUtils.scaleImage(abufferedimage[0], i4);
                            }
                        }

                        TextureMetadataSection texturemetadatasection = iresource
                                .getMetadata("texture");

                        if (texturemetadatasection != null) {
                            List<Integer> list1 = texturemetadatasection.listMipmaps();

                            if (!list1.isEmpty()) {
                                int k1 = abufferedimage[0].getWidth();
                                int l1 = abufferedimage[0].getHeight();

                                if (MathHelper.roundUpToPowerOfTwo(k1) != k1
                                        || MathHelper.roundUpToPowerOfTwo(l1) != l1) {
                                    throw new RuntimeException(
                                            "Unable to load extra miplevels, source-texture is not power of two");
                                }
                            }

                            Iterator iterator1 = list1.iterator();

                            while (iterator1.hasNext()) {
                                int j4 = (Integer) iterator1.next();

                                if (j4 > 0 && j4 < abufferedimage.length - 1 && abufferedimage[j4] == null) {
                                    ResourceLocation resourcelocation = completeResourceLocation(resourcelocation1,
                                            j4);

                                    try {
                                        abufferedimage[j4] = TextureUtil.readBufferedImage(
                                                resourceManager.getResource(resourcelocation).getInputStream());
                                    } catch (IOException ioexception) {
                                        logger.error("Unable to load miplevel {} from: {}",
                                                new Object[]{j4, resourcelocation, ioexception});
                                    }
                                }
                            }
                        }

                        AnimationMetadataSection animationmetadatasection = iresource
                                .getMetadata("animation");
                        textureatlassprite3.loadSprite(abufferedimage, animationmetadatasection);
                    } catch (RuntimeException runtimeexception) {
                        logger.error("Unable to parse metadata from " + resourcelocation2,
                                runtimeexception);

                        continue;
                    } catch (IOException ioexception1) {
                        logger.error("Using missing texture, unable to load " + resourcelocation2 + ", "
                                + ioexception1.getClass().getName());

                        continue;
                    }

                    i = Math.min(i, Math.min(textureatlassprite3.getIconWidth(), textureatlassprite3.getIconHeight()));
                    int j3 = Math.min(Integer.lowestOneBit(textureatlassprite3.getIconWidth()),
                            Integer.lowestOneBit(textureatlassprite3.getIconHeight()));

                    if (j3 < k) {
                        logger.warn("Texture {} with size {}x{} limits mip level from {} to {}",
                                new Object[]{resourcelocation2, textureatlassprite3.getIconWidth(),
                                        textureatlassprite3.getIconHeight(),
                                        MathHelper.calculateLogBaseTwo(k),
                                        MathHelper.calculateLogBaseTwo(j3)});
                        k = j3;
                    }

                    stitcher.addSprite(textureatlassprite3);
                    continue;
                }
            }

            if (l > 0) {
                Config.dbg("Custom loader sprites: " + l);
            }

            if (i1 > 0) {
                Config.dbg("Custom loader sprites (skipped): " + i1);
            }

            int j2 = Math.min(i, k);
            int k2 = MathHelper.calculateLogBaseTwo(j2);

            if (k2 < 0) {
                k2 = 0;
            }

            if (k2 < mipmapLevels) {
                logger.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[]{
                        basePath, mipmapLevels, k2, j2});
                mipmapLevels = k2;
            }

            for (final TextureAtlasSprite textureatlassprite1 : mapRegisteredSprites.values()) {
                if (skipFirst) {
                    break;
                }

                try {
                    textureatlassprite1.generateMipmaps(mipmapLevels);
                } catch (Throwable throwable1) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Applying mipmap");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
                    crashreportcategory.addCrashSectionCallable("Sprite name", new Callable<String>() {
                        public String call() throws Exception {
                            return textureatlassprite1.getIconName();
                        }
                    });
                    crashreportcategory.addCrashSectionCallable("Sprite size", new Callable<String>() {
                        public String call() throws Exception {
                            return textureatlassprite1.getIconWidth() + " x " + textureatlassprite1.getIconHeight();
                        }
                    });
                    crashreportcategory.addCrashSectionCallable("Sprite frames", new Callable<String>() {
                        public String call() throws Exception {
                            return textureatlassprite1.getFrameCount() + " frames";
                        }
                    });
                    crashreportcategory.addCrashSection("Mipmap levels", mipmapLevels);
                    throw new ReportedException(crashreport);
                }
            }

            missingImage.generateMipmaps(mipmapLevels);
            stitcher.addSprite(missingImage);
            skipFirst = false;

            try {
                stitcher.doStitch();
            } catch (StitcherException stitcherexception) {
                throw stitcherexception;
            }

            logger.info("Created: {}x{} {}-atlas", new Object[]{stitcher.getCurrentWidth(),
                    stitcher.getCurrentHeight(), basePath});

            if (Config.isShaders()) {
                ShadersTex.allocateTextureMap(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(),
                        stitcher.getCurrentHeight(), stitcher, this);
            } else {
                TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(),
                        stitcher.getCurrentHeight());
            }

            Map<String, TextureAtlasSprite> map = Maps
                    .newHashMap(mapRegisteredSprites);

            for (TextureAtlasSprite textureatlassprite2 : stitcher.getStichSlots()) {
                String s = textureatlassprite2.getIconName();
                map.remove(s);
                mapUploadedSprites.put(s, textureatlassprite2);

                try {
                    if (Config.isShaders()) {
                        ShadersTex.uploadTexSubForLoadAtlas(this, textureatlassprite2.getIconName(),
                                textureatlassprite2.getFrameTextureData(0), textureatlassprite2.getIconWidth(),
                                textureatlassprite2.getIconHeight(), textureatlassprite2.getOriginX(),
                                textureatlassprite2.getOriginY(), false, false);
                    } else {
                        TextureUtil.uploadTextureMipmap(textureatlassprite2.getFrameTextureData(0),
                                textureatlassprite2.getIconWidth(), textureatlassprite2.getIconHeight(),
                                textureatlassprite2.getOriginX(), textureatlassprite2.getOriginY(), false, false);
                    }
                } catch (Throwable throwable) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
                    CrashReportCategory crashreportcategory1 = crashreport1
                            .makeCategory("Texture being stitched together");
                    crashreportcategory1.addCrashSection("Atlas path", basePath);
                    crashreportcategory1.addCrashSection("Sprite", textureatlassprite2);
                    throw new ReportedException(crashreport1);
                }

                if (textureatlassprite2.hasAnimationMetadata()) {
                    textureatlassprite2.setAnimationIndex(listAnimatedSprites.size());
                    listAnimatedSprites.add(textureatlassprite2);
                }
            }

            for (TextureAtlasSprite textureatlassprite4 : map.values()) {
                textureatlassprite4.copyFrom(missingImage);
            }

            Config.log("Animated sprites: " + listAnimatedSprites.size());

            if (Config.isMultiTexture()) {
                int l2 = stitcher.getCurrentWidth();
                int i3 = stitcher.getCurrentHeight();

                for (TextureAtlasSprite textureatlassprite5 : stitcher.getStichSlots()) {
                    textureatlassprite5.sheetWidth = l2;
                    textureatlassprite5.sheetHeight = i3;
                    textureatlassprite5.mipmapLevels = mipmapLevels;
                    TextureAtlasSprite textureatlassprite6 = textureatlassprite5.spriteSingle;

                    if (textureatlassprite6 != null) {
                        if (textureatlassprite6.getIconWidth() <= 0) {
                            textureatlassprite6.setIconWidth(textureatlassprite5.getIconWidth());
                            textureatlassprite6.setIconHeight(textureatlassprite5.getIconHeight());
                            textureatlassprite6.initSprite(textureatlassprite5.getIconWidth(),
                                    textureatlassprite5.getIconHeight(), 0, 0, false);
                            textureatlassprite6.clearFramesTextureData();
                            List<int[][]> list = textureatlassprite5.getFramesTextureData();
                            textureatlassprite6.setFramesTextureData(list);
                            textureatlassprite6.setAnimationMetadata(textureatlassprite5.getAnimationMetadata());
                        }

                        textureatlassprite6.sheetWidth = l2;
                        textureatlassprite6.sheetHeight = i3;
                        textureatlassprite6.mipmapLevels = mipmapLevels;
                        textureatlassprite6.setAnimationIndex(textureatlassprite5.getAnimationIndex());
                        textureatlassprite5.bindSpriteTexture();
                        boolean flag1 = false;
                        boolean flag = true;

                        try {
                            TextureUtil.uploadTextureMipmap(textureatlassprite6.getFrameTextureData(0),
                                    textureatlassprite6.getIconWidth(), textureatlassprite6.getIconHeight(),
                                    textureatlassprite6.getOriginX(), textureatlassprite6.getOriginY(), flag1, flag);
                        } catch (Exception exception) {
                            Config.dbg("Error uploading sprite single: " + textureatlassprite6 + ", parent: "
                                    + textureatlassprite5);
                            exception.printStackTrace();
                        }
                    }
                }

                Config.getMinecraft().getTextureManager().bindTexture(locationBlocksTexture);
            }

            updateIconGrid(stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

            if (Config.equals(System.getProperty("saveTextureMap"), "true")) {
                Config.dbg("Exporting texture map: " + basePath);
                TextureUtils.saveGlTexture("debug/" + basePath.replaceAll("/", "_"), getGlTextureId(),
                        mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
            }

            return;
        }
    }

    public ResourceLocation completeResourceLocation(ResourceLocation p_completeResourceLocation_1_) {
        return completeResourceLocation(p_completeResourceLocation_1_, 0);
    }

    public ResourceLocation completeResourceLocation(ResourceLocation location, int p_147634_2_) {
        return isAbsoluteLocation(location)
                ? new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".png")
                : (p_147634_2_ == 0
                ? new ResourceLocation(location.getResourceDomain(),
                String.format("%s/%s%s",
                        basePath, location.getResourcePath(), ".png"))
                : new ResourceLocation(location.getResourceDomain(),
                String.format("%s/mipmaps/%s.%d%s", basePath,
                        location.getResourcePath(), p_147634_2_, ".png")));
    }

    public TextureAtlasSprite getAtlasSprite(String iconName) {
        TextureAtlasSprite textureatlassprite = mapUploadedSprites.get(iconName);

        if (textureatlassprite == null) {
            textureatlassprite = missingImage;
        }

        return textureatlassprite;
    }

    public void updateAnimations() {
        boolean flag = false;
        boolean flag1 = false;
        TextureUtil.bindTexture(getGlTextureId());
        int i = 0;

        for (TextureAtlasSprite textureatlassprite : listAnimatedSprites) {
            if (isTerrainAnimationActive(textureatlassprite)) {
                textureatlassprite.updateAnimation();

                if (textureatlassprite.isAnimationActive()) {
                    ++i;
                }

                if (textureatlassprite.spriteNormal != null) {
                    flag = true;
                }

                if (textureatlassprite.spriteSpecular != null) {
                    flag1 = true;
                }
            }
        }

        if (Config.isMultiTexture()) {
            for (TextureAtlasSprite textureatlassprite2 : listAnimatedSprites) {
                if (isTerrainAnimationActive(textureatlassprite2)) {
                    TextureAtlasSprite textureatlassprite1 = textureatlassprite2.spriteSingle;

                    if (textureatlassprite1 != null) {
                        if (textureatlassprite2 == TextureUtils.iconClock
                                || textureatlassprite2 == TextureUtils.iconCompass) {
                            textureatlassprite1.frameCounter = textureatlassprite2.frameCounter;
                        }

                        textureatlassprite2.bindSpriteTexture();
                        textureatlassprite1.updateAnimation();

                        if (textureatlassprite1.isAnimationActive()) {
                            ++i;
                        }
                    }
                }
            }

            TextureUtil.bindTexture(getGlTextureId());
        }

        if (Config.isShaders()) {
            if (flag) {
                TextureUtil.bindTexture(getMultiTexID().norm);

                for (TextureAtlasSprite textureatlassprite3 : listAnimatedSprites) {
                    if (textureatlassprite3.spriteNormal != null
                            && isTerrainAnimationActive(textureatlassprite3)) {
                        if (textureatlassprite3 == TextureUtils.iconClock
                                || textureatlassprite3 == TextureUtils.iconCompass) {
                            textureatlassprite3.spriteNormal.frameCounter = textureatlassprite3.frameCounter;
                        }

                        textureatlassprite3.spriteNormal.updateAnimation();

                        if (textureatlassprite3.spriteNormal.isAnimationActive()) {
                            ++i;
                        }
                    }
                }
            }

            if (flag1) {
                TextureUtil.bindTexture(getMultiTexID().spec);

                for (TextureAtlasSprite textureatlassprite4 : listAnimatedSprites) {
                    if (textureatlassprite4.spriteSpecular != null
                            && isTerrainAnimationActive(textureatlassprite4)) {
                        if (textureatlassprite4 == TextureUtils.iconClock
                                || textureatlassprite4 == TextureUtils.iconCompass) {
                            textureatlassprite4.spriteNormal.frameCounter = textureatlassprite4.frameCounter;
                        }

                        textureatlassprite4.spriteSpecular.updateAnimation();

                        if (textureatlassprite4.spriteSpecular.isAnimationActive()) {
                            ++i;
                        }
                    }
                }
            }

            if (flag || flag1) {
                TextureUtil.bindTexture(getGlTextureId());
            }
        }

        int j = Config.getMinecraft().entityRenderer.frameCount;

        if (j != frameCountAnimations) {
            countAnimationsActive = i;
            frameCountAnimations = j;
        }

        if (SmartAnimations.isActive()) {
            SmartAnimations.resetSpritesRendered();
        }
    }

    public TextureAtlasSprite registerSprite(ResourceLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null!");
        } else {
            TextureAtlasSprite textureatlassprite = mapRegisteredSprites
                    .get(location.toString());

            if (textureatlassprite == null) {
                textureatlassprite = TextureAtlasSprite.makeAtlasSprite(location);
                mapRegisteredSprites.put(location.toString(), textureatlassprite);
                textureatlassprite.updateIndexInMap(counterIndexInMap);

                if (Config.isEmissiveTextures()) {
                    checkEmissive(location, textureatlassprite);
                }
            }

            return textureatlassprite;
        }
    }

    public void tick() {
        updateAnimations();
    }

    public TextureAtlasSprite getMissingSprite() {
        return missingImage;
    }

    public TextureAtlasSprite getTextureExtry(String p_getTextureExtry_1_) {
        return mapRegisteredSprites.get(p_getTextureExtry_1_);
    }

    public boolean setTextureEntry(String p_setTextureEntry_1_, TextureAtlasSprite p_setTextureEntry_2_) {
        if (!mapRegisteredSprites.containsKey(p_setTextureEntry_1_)) {
            mapRegisteredSprites.put(p_setTextureEntry_1_, p_setTextureEntry_2_);
            p_setTextureEntry_2_.updateIndexInMap(counterIndexInMap);
            return true;
        } else {
            return false;
        }
    }

    public boolean setTextureEntry(TextureAtlasSprite p_setTextureEntry_1_) {
        return setTextureEntry(p_setTextureEntry_1_.getIconName(), p_setTextureEntry_1_);
    }

    public String getBasePath() {
        return basePath;
    }

    public int getMipmapLevels() {
        return mipmapLevels;
    }

    public void setMipmapLevels(int mipmapLevelsIn) {
        mipmapLevels = mipmapLevelsIn;
    }

    private boolean isAbsoluteLocation(ResourceLocation p_isAbsoluteLocation_1_) {
        String s = p_isAbsoluteLocation_1_.getResourcePath();
        return isAbsoluteLocationPath(s);
    }

    private boolean isAbsoluteLocationPath(String p_isAbsoluteLocationPath_1_) {
        String s = p_isAbsoluteLocationPath_1_.toLowerCase();
        return s.startsWith("mcpatcher/") || s.startsWith("optifine/");
    }

    public TextureAtlasSprite getSpriteSafe(String p_getSpriteSafe_1_) {
        ResourceLocation resourcelocation = new ResourceLocation(p_getSpriteSafe_1_);
        return mapRegisteredSprites.get(resourcelocation.toString());
    }

    public TextureAtlasSprite getRegisteredSprite(ResourceLocation p_getRegisteredSprite_1_) {
        return mapRegisteredSprites.get(p_getRegisteredSprite_1_.toString());
    }

    private boolean isTerrainAnimationActive(TextureAtlasSprite p_isTerrainAnimationActive_1_) {
        return p_isTerrainAnimationActive_1_ != TextureUtils.iconWaterStill
                && p_isTerrainAnimationActive_1_ != TextureUtils.iconWaterFlow
                ? (p_isTerrainAnimationActive_1_ != TextureUtils.iconLavaStill
                && p_isTerrainAnimationActive_1_ != TextureUtils.iconLavaFlow
                ? (p_isTerrainAnimationActive_1_ != TextureUtils.iconFireLayer0
                && p_isTerrainAnimationActive_1_ != TextureUtils.iconFireLayer1
                ? (p_isTerrainAnimationActive_1_ == TextureUtils.iconPortal
                ? Config.isAnimatedPortal()
                : (p_isTerrainAnimationActive_1_ == TextureUtils.iconClock
                || p_isTerrainAnimationActive_1_ == TextureUtils.iconCompass || Config.isAnimatedTerrain()))
                : Config.isAnimatedFire())
                : Config.isAnimatedLava())
                : Config.isAnimatedWater();
    }

    public int getCountRegisteredSprites() {
        return counterIndexInMap.getValue();
    }

    private int detectMaxMipmapLevel(Map p_detectMaxMipmapLevel_1_, IResourceManager p_detectMaxMipmapLevel_2_) {
        int i = detectMinimumSpriteSize(p_detectMaxMipmapLevel_1_, p_detectMaxMipmapLevel_2_, 20);

        if (i < 16) {
            i = 16;
        }

        i = MathHelper.roundUpToPowerOfTwo(i);

        if (i > 16) {
            Config.log("Sprite size: " + i);
        }

        int j = MathHelper.calculateLogBaseTwo(i);

        if (j < 4) {
            j = 4;
        }

        return j;
    }

    private int detectMinimumSpriteSize(Map p_detectMinimumSpriteSize_1_, IResourceManager p_detectMinimumSpriteSize_2_,
                                        int p_detectMinimumSpriteSize_3_) {
        Map map = new HashMap();

        for (Object o : p_detectMinimumSpriteSize_1_.entrySet()) {
            Entry entry = (Entry) o;
            TextureAtlasSprite textureatlassprite = (TextureAtlasSprite) entry.getValue();
            ResourceLocation resourcelocation = new ResourceLocation(textureatlassprite.getIconName());
            ResourceLocation resourcelocation1 = completeResourceLocation(resourcelocation);

            if (!textureatlassprite.hasCustomLoader(p_detectMinimumSpriteSize_2_, resourcelocation)) {
                try {
                    IResource iresource = p_detectMinimumSpriteSize_2_.getResource(resourcelocation1);

                    if (iresource != null) {
                        InputStream inputstream = iresource.getInputStream();

                        if (inputstream != null) {
                            Dimension dimension = TextureUtils.getImageSize(inputstream, "png");
                            inputstream.close();

                            if (dimension != null) {
                                int i = dimension.width;
                                int j = MathHelper.roundUpToPowerOfTwo(i);

                                if (!map.containsKey(j)) {
                                    map.put(j, 1);
                                } else {
                                    int k = (Integer) map.get(j);
                                    map.put(j, k + 1);
                                }
                            }
                        }
                    }
                } catch (Exception var17) {
                }
            }
        }

        int l = 0;
        Set set = map.keySet();
        Set set1 = new TreeSet(set);
        int l1;

        for (Iterator iterator = set1.iterator(); iterator.hasNext(); l += l1) {
            int j1 = (Integer) iterator.next();
            l1 = (Integer) map.get(j1);
        }

        int i1 = 16;
        int k1 = 0;
        l1 = l * p_detectMinimumSpriteSize_3_ / 100;
        Iterator iterator1 = set1.iterator();

        while (iterator1.hasNext()) {
            int i2 = (Integer) iterator1.next();
            int j2 = (Integer) map.get(i2);
            k1 += j2;

            if (i2 > i1) {
                i1 = i2;
            }

            if (k1 > l1) {
                return i1;
            }
        }

        return i1;
    }

    private int getMinSpriteSize() {
        int i = 1 << mipmapLevels;

        if (i < 8) {
            i = 8;
        }

        return i;
    }

    private int[] getMissingImageData(int p_getMissingImageData_1_) {
        BufferedImage bufferedimage = new BufferedImage(16, 16, 2);
        bufferedimage.setRGB(0, 0, 16, 16, TextureUtil.missingTextureData, 0, 16);
        BufferedImage bufferedimage1 = TextureUtils.scaleImage(bufferedimage, p_getMissingImageData_1_);
        int[] aint = new int[p_getMissingImageData_1_ * p_getMissingImageData_1_];
        bufferedimage1.getRGB(0, 0, p_getMissingImageData_1_, p_getMissingImageData_1_, aint, 0,
                p_getMissingImageData_1_);
        return aint;
    }

    public boolean isTextureBound() {
        int i = GlStateManager.getBoundTexture();
        int j = getGlTextureId();
        return i == j;
    }

    private void updateIconGrid(int p_updateIconGrid_1_, int p_updateIconGrid_2_) {
        iconGridCountX = -1;
        iconGridCountY = -1;
        iconGrid = null;

        if (iconGridSize > 0) {
            iconGridCountX = p_updateIconGrid_1_ / iconGridSize;
            iconGridCountY = p_updateIconGrid_2_ / iconGridSize;
            iconGrid = new TextureAtlasSprite[iconGridCountX * iconGridCountY];
            iconGridSizeU = 1.0D / (double) iconGridCountX;
            iconGridSizeV = 1.0D / (double) iconGridCountY;

            for (TextureAtlasSprite textureatlassprite : mapUploadedSprites.values()) {
                double d0 = 0.5D / (double) p_updateIconGrid_1_;
                double d1 = 0.5D / (double) p_updateIconGrid_2_;
                double d2 = (double) Math.min(textureatlassprite.getMinU(), textureatlassprite.getMaxU()) + d0;
                double d3 = (double) Math.min(textureatlassprite.getMinV(), textureatlassprite.getMaxV()) + d1;
                double d4 = (double) Math.max(textureatlassprite.getMinU(), textureatlassprite.getMaxU()) - d0;
                double d5 = (double) Math.max(textureatlassprite.getMinV(), textureatlassprite.getMaxV()) - d1;
                int i = (int) (d2 / iconGridSizeU);
                int j = (int) (d3 / iconGridSizeV);
                int k = (int) (d4 / iconGridSizeU);
                int l = (int) (d5 / iconGridSizeV);

                for (int i1 = i; i1 <= k; ++i1) {
                    if (i1 >= 0 && i1 < iconGridCountX) {
                        for (int j1 = j; j1 <= l; ++j1) {
                            if (j1 >= 0 && j1 < iconGridCountX) {
                                int k1 = j1 * iconGridCountX + i1;
                                iconGrid[k1] = textureatlassprite;
                            } else {
                                Config.warn("Invalid grid V: " + j1 + ", icon: " + textureatlassprite.getIconName());
                            }
                        }
                    } else {
                        Config.warn("Invalid grid U: " + i1 + ", icon: " + textureatlassprite.getIconName());
                    }
                }
            }
        }
    }

    public TextureAtlasSprite getIconByUV(double p_getIconByUV_1_, double p_getIconByUV_3_) {
        if (iconGrid == null) {
            return null;
        } else {
            int i = (int) (p_getIconByUV_1_ / iconGridSizeU);
            int j = (int) (p_getIconByUV_3_ / iconGridSizeV);
            int k = j * iconGridCountX + i;
            return k >= 0 && k <= iconGrid.length ? iconGrid[k] : null;
        }
    }

    private void checkEmissive(ResourceLocation p_checkEmissive_1_, TextureAtlasSprite p_checkEmissive_2_) {
        String s = EmissiveTextures.getSuffixEmissive();

        if (s != null) {
            if (!p_checkEmissive_1_.getResourcePath().endsWith(s)) {
                ResourceLocation resourcelocation = new ResourceLocation(p_checkEmissive_1_.getResourceDomain(),
                        p_checkEmissive_1_.getResourcePath() + s);
                ResourceLocation resourcelocation1 = completeResourceLocation(resourcelocation);

                if (Config.hasResource(resourcelocation1)) {
                    TextureAtlasSprite textureatlassprite = registerSprite(resourcelocation);
                    textureatlassprite.isEmissive = true;
                    p_checkEmissive_2_.spriteEmissive = textureatlassprite;
                }
            }
        }
    }

    public int getCountAnimations() {
        return listAnimatedSprites.size();
    }

    public int getCountAnimationsActive() {
        return countAnimationsActive;
    }
}
