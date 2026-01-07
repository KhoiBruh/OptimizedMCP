package net.optifine;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Properties;

public class TextureAnimation {
    final ResourceLocation dstTexLoc;
    byte[] srcData;
    private final String dstTex;
    private int dstTextId = -1;
    private final int dstX;
    private final int dstY;
    private final int frameWidth;
    private final int frameHeight;
    private final TextureAnimationFrame[] frames;
    private int currentFrameIndex = 0;
    private final boolean interpolate;
    private final int interpolateSkip;
    private ByteBuffer interpolateData = null;
    private ByteBuffer imageData = null;
    private boolean active = true;
    private boolean valid = true;

    public TextureAnimation(String texFrom, byte[] srcData, String texTo, ResourceLocation locTexTo, int dstX, int dstY, int frameWidth, int frameHeight, Properties props) {
        dstTex = texTo;
        dstTexLoc = locTexTo;
        this.dstX = dstX;
        this.dstY = dstY;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        int i = frameWidth * frameHeight * 4;

        if (srcData.length % i != 0) {
            Config.warn("Invalid animated texture length: " + srcData.length + ", frameWidth: " + frameWidth + ", frameHeight: " + frameHeight);
        }

        this.srcData = srcData;
        int j = srcData.length / i;

        if (props.get("tile.0") != null) {
            for (int k = 0; props.get("tile." + k) != null; ++k) {
                j = k + 1;
            }
        }

        String s2 = (String) props.get("duration");
        int l = Math.max(Config.parseInt(s2, 1), 1);
        frames = new TextureAnimationFrame[j];

        for (int i1 = 0; i1 < frames.length; ++i1) {
            String s = (String) props.get("tile." + i1);
            int j1 = Config.parseInt(s, i1);
            String s1 = (String) props.get("duration." + i1);
            int k1 = Math.max(Config.parseInt(s1, l), 1);
            TextureAnimationFrame textureanimationframe = new TextureAnimationFrame(j1, k1);
            frames[i1] = textureanimationframe;
        }

        interpolate = Config.parseBoolean(props.getProperty("interpolate"), false);
        interpolateSkip = Config.parseInt(props.getProperty("skip"), 0);

        if (interpolate) {
            interpolateData = GLAllocation.createDirectByteBuffer(i);
        }
    }

    public boolean nextFrame() {
        TextureAnimationFrame textureanimationframe = getCurrentFrame();

        if (textureanimationframe == null) {
            return false;
        } else {
            ++textureanimationframe.counter;

            if (textureanimationframe.counter < textureanimationframe.duration) {
                return interpolate;
            } else {
                textureanimationframe.counter = 0;
                ++currentFrameIndex;

                if (currentFrameIndex >= frames.length) {
                    currentFrameIndex = 0;
                }

                return true;
            }
        }
    }

    public TextureAnimationFrame getCurrentFrame() {
        return getFrame(currentFrameIndex);
    }

    public TextureAnimationFrame getFrame(int index) {
        if (frames.length == 0) {
            return null;
        } else {
            if (index < 0 || index >= frames.length) {
                index = 0;
            }

            return frames[index];
        }
    }

    public void updateTexture() {
        if (valid) {
            if (dstTextId < 0) {
                ITextureObject itextureobject = TextureUtils.getTexture(dstTexLoc);

                if (itextureobject == null) {
                    valid = false;
                    return;
                }

                dstTextId = itextureobject.getGlTextureId();
            }

            if (imageData == null) {
                imageData = GLAllocation.createDirectByteBuffer(srcData.length);
                imageData.put(srcData);
                imageData.flip();
                srcData = null;
            }

            active = !SmartAnimations.isActive() || SmartAnimations.isTextureRendered(dstTextId);

            if (nextFrame()) {
                if (active) {
                    int j = frameWidth * frameHeight * 4;
                    TextureAnimationFrame textureanimationframe = getCurrentFrame();

                    if (textureanimationframe != null) {
                        int i = j * textureanimationframe.index;

                        if (i + j <= imageData.limit()) {
                            if (interpolate && textureanimationframe.counter > 0) {
                                if (interpolateSkip <= 1 || textureanimationframe.counter % interpolateSkip == 0) {
                                    TextureAnimationFrame textureanimationframe1 = getFrame(currentFrameIndex + 1);
                                    double d0 = (double) textureanimationframe.counter / (double) textureanimationframe.duration;
                                    updateTextureInerpolate(textureanimationframe, textureanimationframe1, d0);
                                }
                            } else {
                                imageData.position(i);
                                GlStateManager.bindTexture(dstTextId);
                                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, dstX, dstY, frameWidth, frameHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageData);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateTextureInerpolate(TextureAnimationFrame frame1, TextureAnimationFrame frame2, double dd) {
        int i = frameWidth * frameHeight * 4;
        int j = i * frame1.index;

        if (j + i <= imageData.limit()) {
            int k = i * frame2.index;

            if (k + i <= imageData.limit()) {
                interpolateData.clear();

                for (int l = 0; l < i; ++l) {
                    int i1 = imageData.get(j + l) & 255;
                    int j1 = imageData.get(k + l) & 255;
                    int k1 = mix(i1, j1, dd);
                    byte b0 = (byte) k1;
                    interpolateData.put(b0);
                }

                interpolateData.flip();
                GlStateManager.bindTexture(dstTextId);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, dstX, dstY, frameWidth, frameHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, interpolateData);
            }
        }
    }

    private int mix(int col1, int col2, double k) {
        return (int) ((double) col1 * (1.0D - k) + (double) col2 * k);
    }

    public String getDstTex() {
        return dstTex;
    }

    public boolean isActive() {
        return active;
    }
}
