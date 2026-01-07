package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.optifine.SmartAnimations;
import net.optifine.shaders.Shaders;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class TextureAtlasSprite {
    private static String locationNameClock = "builtin/clock";
    private static String locationNameCompass = "builtin/compass";
    private final String iconName;
    public float baseU;
    public float baseV;
    public int sheetWidth;
    public int sheetHeight;
    public int glSpriteTextureId = -1;
    public TextureAtlasSprite spriteSingle = null;
    public boolean isSpriteSingle = false;
    public int mipmapLevels = 0;
    public TextureAtlasSprite spriteNormal = null;
    public TextureAtlasSprite spriteSpecular = null;
    public boolean isShadersSprite = false;
    public boolean isEmissive = false;
    public TextureAtlasSprite spriteEmissive = null;
    protected List<int[][]> framesTextureData = Lists.newArrayList();
    protected int[][] interpolatedFrameData;
    protected boolean rotated;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    protected int frameCounter;
    protected int tickCounter;
    private AnimationMetadataSection animationMetadata;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    private int indexInMap = -1;
    private int animationIndex = -1;
    private boolean animationActive = false;

    private TextureAtlasSprite(String p_i7_1_, boolean p_i7_2_) {
        iconName = p_i7_1_;
        isSpriteSingle = p_i7_2_;
    }

    public TextureAtlasSprite(String spriteName) {
        iconName = spriteName;

        if (Config.isMultiTexture()) {
            spriteSingle = new TextureAtlasSprite(getIconName() + ".spriteSingle", true);
        }
    }

    protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation spriteResourceLocation) {
        String s = spriteResourceLocation.toString();
        return locationNameClock.equals(s) ? new TextureClock(s) : (locationNameCompass.equals(s) ? new TextureCompass(s) : new TextureAtlasSprite(s));
    }

    public static void setLocationNameClock(String clockName) {
        locationNameClock = clockName;
    }

    public static void setLocationNameCompass(String compassName) {
        locationNameCompass = compassName;
    }

    private static int[][] getFrameTextureData(int[][] data, int rows, int columns, int p_147962_3_) {
        int[][] aint = new int[data.length][];

        for (int i = 0; i < data.length; ++i) {
            int[] aint1 = data[i];

            if (aint1 != null) {
                aint[i] = new int[(rows >> i) * (columns >> i)];
                System.arraycopy(aint1, p_147962_3_ * aint[i].length, aint[i], 0, aint[i].length);
            }
        }

        return aint;
    }

    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn) {
        originX = originInX;
        originY = originInY;
        rotated = rotatedIn;
        float f = (float) (0.009999999776482582D / (double) inX);
        float f1 = (float) (0.009999999776482582D / (double) inY);
        minU = (float) originInX / (float) ((double) inX) + f;
        maxU = (float) (originInX + width) / (float) ((double) inX) - f;
        minV = (float) originInY / (float) inY + f1;
        maxV = (float) (originInY + height) / (float) inY - f1;
        baseU = Math.min(minU, maxU);
        baseV = Math.min(minV, maxV);

        if (spriteSingle != null) {
            spriteSingle.initSprite(width, height, 0, 0, false);
        }

        if (spriteNormal != null) {
            spriteNormal.copyFrom(this);
        }

        if (spriteSpecular != null) {
            spriteSpecular.copyFrom(this);
        }
    }

    public void copyFrom(TextureAtlasSprite atlasSpirit) {
        originX = atlasSpirit.originX;
        originY = atlasSpirit.originY;
        width = atlasSpirit.width;
        height = atlasSpirit.height;
        rotated = atlasSpirit.rotated;
        minU = atlasSpirit.minU;
        maxU = atlasSpirit.maxU;
        minV = atlasSpirit.minV;
        maxV = atlasSpirit.maxV;

        if (atlasSpirit != Config.getTextureMap().getMissingSprite()) {
            indexInMap = atlasSpirit.indexInMap;
        }

        baseU = atlasSpirit.baseU;
        baseV = atlasSpirit.baseV;
        sheetWidth = atlasSpirit.sheetWidth;
        sheetHeight = atlasSpirit.sheetHeight;
        glSpriteTextureId = atlasSpirit.glSpriteTextureId;
        mipmapLevels = atlasSpirit.mipmapLevels;

        if (spriteSingle != null) {
            spriteSingle.initSprite(width, height, 0, 0, false);
        }

        animationIndex = atlasSpirit.animationIndex;
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    public int getIconWidth() {
        return width;
    }

    public void setIconWidth(int newWidth) {
        width = newWidth;

        if (spriteSingle != null) {
            spriteSingle.setIconWidth(width);
        }
    }

    public int getIconHeight() {
        return height;
    }

    public void setIconHeight(int newHeight) {
        height = newHeight;

        if (spriteSingle != null) {
            spriteSingle.setIconHeight(height);
        }
    }

    public float getMinU() {
        return minU;
    }

    public float getMaxU() {
        return maxU;
    }

    public float getInterpolatedU(double u) {
        float f = maxU - minU;
        return minU + f * (float) u / 16.0F;
    }

    public float getMinV() {
        return minV;
    }

    public float getMaxV() {
        return maxV;
    }

    public float getInterpolatedV(double v) {
        float f = maxV - minV;
        return minV + f * ((float) v / 16.0F);
    }

    public String getIconName() {
        return iconName;
    }

    public void updateAnimation() {
        if (animationMetadata != null) {
            animationActive = !SmartAnimations.isActive() || SmartAnimations.isSpriteRendered(animationIndex);
            ++tickCounter;

            if (tickCounter >= animationMetadata.getFrameTimeSingle(frameCounter)) {
                int i = animationMetadata.getFrameIndex(frameCounter);
                int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
                frameCounter = (frameCounter + 1) % j;
                tickCounter = 0;
                int k = animationMetadata.getFrameIndex(frameCounter);
                boolean flag = false;
                boolean flag1 = isSpriteSingle;

                if (!animationActive) {
                    return;
                }

                if (i != k && k >= 0 && k < framesTextureData.size()) {
                    TextureUtil.uploadTextureMipmap(framesTextureData.get(k), width, height, originX, originY, flag, flag1);
                }
            } else if (animationMetadata.isInterpolate()) {
                if (!animationActive) {
                    return;
                }

                updateAnimationInterpolated();
            }
        }
    }

    private void updateAnimationInterpolated() {
        double d0 = 1.0D - (double) tickCounter / (double) animationMetadata.getFrameTimeSingle(frameCounter);
        int i = animationMetadata.getFrameIndex(frameCounter);
        int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
        int k = animationMetadata.getFrameIndex((frameCounter + 1) % j);

        if (i != k && k >= 0 && k < framesTextureData.size()) {
            int[][] aint = framesTextureData.get(i);
            int[][] aint1 = framesTextureData.get(k);

            if (interpolatedFrameData == null || interpolatedFrameData.length != aint.length) {
                interpolatedFrameData = new int[aint.length][];
            }

            for (int l = 0; l < aint.length; ++l) {
                if (interpolatedFrameData[l] == null) {
                    interpolatedFrameData[l] = new int[aint[l].length];
                }

                if (l < aint1.length && aint1[l].length == aint[l].length) {
                    for (int i1 = 0; i1 < aint[l].length; ++i1) {
                        int j1 = aint[l][i1];
                        int k1 = aint1[l][i1];
                        int l1 = (int) ((double) ((j1 & 16711680) >> 16) * d0 + (double) ((k1 & 16711680) >> 16) * (1.0D - d0));
                        int i2 = (int) ((double) ((j1 & 65280) >> 8) * d0 + (double) ((k1 & 65280) >> 8) * (1.0D - d0));
                        int j2 = (int) ((double) (j1 & 255) * d0 + (double) (k1 & 255) * (1.0D - d0));
                        interpolatedFrameData[l][i1] = j1 & -16777216 | l1 << 16 | i2 << 8 | j2;
                    }
                }
            }

            TextureUtil.uploadTextureMipmap(interpolatedFrameData, width, height, originX, originY, false, false);
        }
    }

    public int[][] getFrameTextureData(int index) {
        return framesTextureData.get(index);
    }

    public int getFrameCount() {
        return framesTextureData.size();
    }

    public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta) {
        resetSprite();
        int i = images[0].getWidth();
        int j = images[0].getHeight();
        width = i;
        height = j;

        if (spriteSingle != null) {
            spriteSingle.width = width;
            spriteSingle.height = height;
        }

        int[][] aint = new int[images.length][];

        for (int k = 0; k < images.length; ++k) {
            BufferedImage bufferedimage = images[k];

            if (bufferedimage != null) {
                if (width >> k != bufferedimage.getWidth()) {
                    bufferedimage = TextureUtils.scaleImage(bufferedimage, width >> k);
                }

                if (k > 0 && (bufferedimage.getWidth() != i >> k || bufferedimage.getHeight() != j >> k)) {
                    throw new RuntimeException(String.format("Unable to load miplevel: %d, image is size: %dx%d, expected %dx%d", k, bufferedimage.getWidth(), bufferedimage.getHeight(), i >> k, j >> k));
                }

                aint[k] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
                bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[k], 0, bufferedimage.getWidth());
            }
        }

        if (meta == null) {
            if (j != i) {
                throw new RuntimeException("broken aspect ratio and not an animation");
            }

            framesTextureData.add(aint);
        } else {
            int j1 = j / i;
            height = width;

            if (meta.getFrameCount() > 0) {

                for (int i1 : meta.getFrameIndexSet()) {
                    if (i1 >= j1) {
                        throw new RuntimeException("invalid frameindex " + i1);
                    }

                    allocateFrameTextureData(i1);
                    framesTextureData.set(i1, getFrameTextureData(aint, i, i, i1));
                }

                animationMetadata = meta;
            } else {
                List<AnimationFrame> list = Lists.newArrayList();

                for (int j2 = 0; j2 < j1; ++j2) {
                    framesTextureData.add(getFrameTextureData(aint, i, i, j2));
                    list.add(new AnimationFrame(j2, -1));
                }

                animationMetadata = new AnimationMetadataSection(list, width, height, meta.getFrameTime(), meta.isInterpolate());
            }
        }

        if (!isShadersSprite) {
            if (Config.isShaders()) {
                loadShadersSprites();
            }

            for (int[][] aint1 : framesTextureData) {
                if (aint1 != null && !iconName.startsWith("minecraft:blocks/leaves_")) {
                    for (int[] aint2 : aint1) {
                        fixTransparentColor(aint2);
                    }
                }
            }

            if (spriteSingle != null) {
                spriteSingle.loadSprite(images, meta);
            }
        }
    }

    public void generateMipmaps(int level) {
        List<int[][]> list = Lists.newArrayList();

        for (int i = 0; i < framesTextureData.size(); ++i) {
            final int[][] aint = framesTextureData.get(i);

            if (aint != null) {
                try {
                    list.add(TextureUtil.generateMipmapData(level, width, aint));
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Generating mipmaps for frame");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Frame being iterated");
                    crashreportcategory.addCrashSection("Frame index", i);
                    crashreportcategory.addCrashSectionCallable("Frame sizes", () -> {
                        StringBuilder stringbuilder = new StringBuilder();

                        for (int[] aint1 : aint) {
                            if (!stringbuilder.isEmpty()) {
                                stringbuilder.append(", ");
                            }

                            stringbuilder.append(aint1 == null ? "null" : Integer.valueOf(aint1.length));
                        }

                        return stringbuilder.toString();
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        setFramesTextureData(list);

        if (spriteSingle != null) {
            spriteSingle.generateMipmaps(level);
        }
    }

    private void allocateFrameTextureData(int index) {
        if (framesTextureData.size() <= index) {
            for (int i = framesTextureData.size(); i <= index; ++i) {
                framesTextureData.add(null);
            }
        }

        if (spriteSingle != null) {
            spriteSingle.allocateFrameTextureData(index);
        }
    }

    public void clearFramesTextureData() {
        framesTextureData.clear();

        if (spriteSingle != null) {
            spriteSingle.clearFramesTextureData();
        }
    }

    public boolean hasAnimationMetadata() {
        return animationMetadata != null;
    }

    private void resetSprite() {
        animationMetadata = null;
        setFramesTextureData(Lists.newArrayList());
        frameCounter = 0;
        tickCounter = 0;

        if (spriteSingle != null) {
            spriteSingle.resetSprite();
        }
    }

    public String toString() {
        return "TextureAtlasSprite{name='" + iconName + '\'' + ", frameCount=" + framesTextureData.size() + ", rotated=" + rotated + ", x=" + originX + ", y=" + originY + ", height=" + height + ", width=" + width + ", u0=" + minU + ", u1=" + maxU + ", v0=" + minV + ", v1=" + maxV + '}';
    }

    public boolean hasCustomLoader(IResourceManager p_hasCustomLoader_1_, ResourceLocation p_hasCustomLoader_2_) {
        return false;
    }

    public boolean load(IResourceManager p_load_1_, ResourceLocation p_load_2_) {
        return true;
    }

    public int getIndexInMap() {
        return indexInMap;
    }

    public void setIndexInMap(int p_setIndexInMap_1_) {
        indexInMap = p_setIndexInMap_1_;
    }

    public void updateIndexInMap(CounterInt p_updateIndexInMap_1_) {
        if (indexInMap < 0) {
            indexInMap = p_updateIndexInMap_1_.nextValue();
        }
    }

    public int getAnimationIndex() {
        return animationIndex;
    }

    public void setAnimationIndex(int p_setAnimationIndex_1_) {
        animationIndex = p_setAnimationIndex_1_;

        if (spriteNormal != null) {
            spriteNormal.setAnimationIndex(p_setAnimationIndex_1_);
        }

        if (spriteSpecular != null) {
            spriteSpecular.setAnimationIndex(p_setAnimationIndex_1_);
        }
    }

    public boolean isAnimationActive() {
        return animationActive;
    }

    private void fixTransparentColor(int[] p_fixTransparentColor_1_) {
        if (p_fixTransparentColor_1_ != null) {
            long i = 0L;
            long j = 0L;
            long k = 0L;
            long l = 0L;

            for (int j1 : p_fixTransparentColor_1_) {
                int k1 = j1 >> 24 & 255;

                if (k1 >= 16) {
                    int l1 = j1 >> 16 & 255;
                    int i2 = j1 >> 8 & 255;
                    int j2 = j1 & 255;
                    i += l1;
                    j += i2;
                    k += j2;
                    ++l;
                }
            }

            if (l > 0L) {
                int l2 = (int) (i / l);
                int i3 = (int) (j / l);
                int j3 = (int) (k / l);
                int k3 = l2 << 16 | i3 << 8 | j3;

                for (int l3 = 0; l3 < p_fixTransparentColor_1_.length; ++l3) {
                    int i4 = p_fixTransparentColor_1_[l3];
                    int k2 = i4 >> 24 & 255;

                    if (k2 <= 16) {
                        p_fixTransparentColor_1_[l3] = k3;
                    }
                }
            }
        }
    }

    public double getSpriteU16(float p_getSpriteU16_1_) {
        float f = maxU - minU;
        return (p_getSpriteU16_1_ - minU) / f * 16.0F;
    }

    public double getSpriteV16(float p_getSpriteV16_1_) {
        float f = maxV - minV;
        return (p_getSpriteV16_1_ - minV) / f * 16.0F;
    }

    public void bindSpriteTexture() {
        if (glSpriteTextureId < 0) {
            glSpriteTextureId = TextureUtil.glGenTextures();
            TextureUtil.allocateTextureImpl(glSpriteTextureId, mipmapLevels, width, height);
            TextureUtils.applyAnisotropicLevel();
        }

        TextureUtils.bindTexture(glSpriteTextureId);
    }

    public void deleteSpriteTexture() {
        if (glSpriteTextureId >= 0) {
            TextureUtil.deleteTexture(glSpriteTextureId);
            glSpriteTextureId = -1;
        }
    }

    public float toSingleU(float p_toSingleU_1_) {
        p_toSingleU_1_ = p_toSingleU_1_ - baseU;
        float f = (float) sheetWidth / (float) width;
        p_toSingleU_1_ = p_toSingleU_1_ * f;
        return p_toSingleU_1_;
    }

    public float toSingleV(float p_toSingleV_1_) {
        p_toSingleV_1_ = p_toSingleV_1_ - baseV;
        float f = (float) sheetHeight / (float) height;
        p_toSingleV_1_ = p_toSingleV_1_ * f;
        return p_toSingleV_1_;
    }

    public List<int[][]> getFramesTextureData() {
        List<int[][]> list = new ArrayList<>(framesTextureData);
        return list;
    }

    public void setFramesTextureData(List<int[][]> newFramesTextureData) {
        framesTextureData = newFramesTextureData;

        if (spriteSingle != null) {
            spriteSingle.setFramesTextureData(newFramesTextureData);
        }
    }

    public AnimationMetadataSection getAnimationMetadata() {
        return animationMetadata;
    }

    public void setAnimationMetadata(AnimationMetadataSection p_setAnimationMetadata_1_) {
        animationMetadata = p_setAnimationMetadata_1_;
    }

    private void loadShadersSprites() {
        if (Shaders.configNormalMap) {
            String s = iconName + "_n";
            ResourceLocation resourcelocation = new ResourceLocation(s);
            resourcelocation = Config.getTextureMap().completeResourceLocation(resourcelocation);

            if (Config.hasResource(resourcelocation)) {
                spriteNormal = new TextureAtlasSprite(s);
                spriteNormal.isShadersSprite = true;
                spriteNormal.copyFrom(this);
                spriteNormal.generateMipmaps(mipmapLevels);
            }
        }

        if (Shaders.configSpecularMap) {
            String s1 = iconName + "_s";
            ResourceLocation resourcelocation1 = new ResourceLocation(s1);
            resourcelocation1 = Config.getTextureMap().completeResourceLocation(resourcelocation1);

            if (Config.hasResource(resourcelocation1)) {
                spriteSpecular = new TextureAtlasSprite(s1);
                spriteSpecular.isShadersSprite = true;
                spriteSpecular.copyFrom(this);
                spriteSpecular.generateMipmaps(mipmapLevels);
            }
        }
    }
}
