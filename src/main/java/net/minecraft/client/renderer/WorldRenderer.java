package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.optifine.SmartAnimations;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import java.nio.*;
import java.util.Arrays;
import java.util.BitSet;

public class WorldRenderer {
    public IntBuffer rawIntBuffer;
    public FloatBuffer rawFloatBuffer;
    public int vertexCount;
    public int drawMode;
    public SVertexBuilder sVertexBuilder;
    public RenderEnv renderEnv = null;
    public BitSet animatedSprites = null;
    public BitSet animatedSpritesCached = new BitSet();
    private ByteBuffer byteBuffer;
    private ShortBuffer rawShortBuffer;
    private VertexFormatElement vertexFormatElement;
    private int vertexFormatIndex;
    private boolean noColor;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private VertexFormat vertexFormat;
    private boolean isDrawing;
    private EnumWorldBlockLayer blockLayer = null;
    private boolean[] drawnIcons = new boolean[256];
    private TextureAtlasSprite[] quadSprites = null;
    private TextureAtlasSprite[] quadSpritesPrev = null;
    private TextureAtlasSprite quadSprite = null;
    private boolean modeTriangles = false;
    private ByteBuffer byteBufferTriangles;

    public WorldRenderer(int bufferSizeIn) {
        byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
        rawIntBuffer = byteBuffer.asIntBuffer();
        rawShortBuffer = byteBuffer.asShortBuffer();
        rawFloatBuffer = byteBuffer.asFloatBuffer();
        SVertexBuilder.initVertexBuilder(this);
    }

    private static float getDistanceSq(FloatBuffer p_181665_0_, float p_181665_1_, float p_181665_2_, float p_181665_3_, int p_181665_4_, int p_181665_5_) {
        float f = p_181665_0_.get(p_181665_5_);
        float f1 = p_181665_0_.get(p_181665_5_ + 1);
        float f2 = p_181665_0_.get(p_181665_5_ + 2);
        float f3 = p_181665_0_.get(p_181665_5_ + p_181665_4_);
        float f4 = p_181665_0_.get(p_181665_5_ + p_181665_4_ + 1);
        float f5 = p_181665_0_.get(p_181665_5_ + p_181665_4_ + 2);
        float f6 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2);
        float f7 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 1);
        float f8 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 2);
        float f9 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3);
        float f10 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 1);
        float f11 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 2);
        float f12 = (f + f3 + f6 + f9) * 0.25F - p_181665_1_;
        float f13 = (f1 + f4 + f7 + f10) * 0.25F - p_181665_2_;
        float f14 = (f2 + f5 + f8 + f11) * 0.25F - p_181665_3_;
        return f12 * f12 + f13 * f13 + f14 * f14;
    }

    private void growBuffer(int p_181670_1_) {
        if (p_181670_1_ > rawIntBuffer.remaining()) {
            int i = byteBuffer.capacity();
            int j = i % 2097152;
            int k = j + (((rawIntBuffer.position() + p_181670_1_) * 4 - j) / 2097152 + 1) * 2097152;
            LogManager.getLogger().warn("Needed to grow BufferBuilder buffer: Old size " + i + " bytes, new size " + k + " bytes.");
            int l = rawIntBuffer.position();
            ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(k);
            byteBuffer.position(0);
            bytebuffer.put(byteBuffer);
            bytebuffer.rewind();
            byteBuffer = bytebuffer;
            rawFloatBuffer = byteBuffer.asFloatBuffer();
            rawIntBuffer = byteBuffer.asIntBuffer();
            rawIntBuffer.position(l);
            rawShortBuffer = byteBuffer.asShortBuffer();
            rawShortBuffer.position(l << 1);

            if (quadSprites != null) {
                TextureAtlasSprite[] atextureatlassprite = quadSprites;
                int i1 = getBufferQuadSize();
                quadSprites = new TextureAtlasSprite[i1];
                System.arraycopy(atextureatlassprite, 0, quadSprites, 0, Math.min(atextureatlassprite.length, quadSprites.length));
                quadSpritesPrev = null;
            }
        }
    }

    public void sortVertexData(float p_181674_1_, float p_181674_2_, float p_181674_3_) {
        int i = vertexCount / 4;
        final float[] afloat = new float[i];

        for (int j = 0; j < i; ++j) {
            afloat[j] = getDistanceSq(rawFloatBuffer, (float) ((double) p_181674_1_ + xOffset), (float) ((double) p_181674_2_ + yOffset), (float) ((double) p_181674_3_ + zOffset), vertexFormat.getIntegerSize(), j * vertexFormat.getNextOffset());
        }

        Integer[] ainteger = new Integer[i];

        for (int k = 0; k < ainteger.length; ++k) {
            ainteger[k] = k;
        }

        Arrays.sort(ainteger, (p_compare_1_, p_compare_2_) -> Floats.compare(afloat[p_compare_2_], afloat[p_compare_1_]));
        BitSet bitset = new BitSet();
        int l = vertexFormat.getNextOffset();
        int[] aint = new int[l];

        for (int l1 = 0; (l1 = bitset.nextClearBit(l1)) < ainteger.length; ++l1) {
            int i1 = ainteger[l1];

            if (i1 != l1) {
                rawIntBuffer.limit(i1 * l + l);
                rawIntBuffer.position(i1 * l);
                rawIntBuffer.get(aint);
                int j1 = i1;

                for (int k1 = ainteger[i1]; j1 != l1; k1 = ainteger[k1]) {
                    rawIntBuffer.limit(k1 * l + l);
                    rawIntBuffer.position(k1 * l);
                    IntBuffer intbuffer = rawIntBuffer.slice();
                    rawIntBuffer.limit(j1 * l + l);
                    rawIntBuffer.position(j1 * l);
                    rawIntBuffer.put(intbuffer);
                    bitset.set(j1);
                    j1 = k1;
                }

                rawIntBuffer.limit(l1 * l + l);
                rawIntBuffer.position(l1 * l);
                rawIntBuffer.put(aint);
            }

            bitset.set(l1);
        }

        rawIntBuffer.limit(rawIntBuffer.capacity());
        rawIntBuffer.position(getBufferSize());

        if (quadSprites != null) {
            TextureAtlasSprite[] atextureatlassprite = new TextureAtlasSprite[vertexCount / 4];
            int i2 = vertexFormat.getNextOffset() / 4 * 4;

            for (int j2 = 0; j2 < ainteger.length; ++j2) {
                int k2 = ainteger[j2];
                atextureatlassprite[j2] = quadSprites[k2];
            }

            System.arraycopy(atextureatlassprite, 0, quadSprites, 0, atextureatlassprite.length);
        }
    }

    public WorldRenderer.State getVertexState() {
        rawIntBuffer.rewind();
        int i = getBufferSize();
        rawIntBuffer.limit(i);
        int[] aint = new int[i];
        rawIntBuffer.get(aint);
        rawIntBuffer.limit(rawIntBuffer.capacity());
        rawIntBuffer.position(i);
        TextureAtlasSprite[] atextureatlassprite = null;

        if (quadSprites != null) {
            int j = vertexCount / 4;
            atextureatlassprite = new TextureAtlasSprite[j];
            System.arraycopy(quadSprites, 0, atextureatlassprite, 0, j);
        }

        return new WorldRenderer.State(aint, new VertexFormat(vertexFormat), atextureatlassprite);
    }

    public void setVertexState(WorldRenderer.State state) {
        rawIntBuffer.clear();
        growBuffer(state.getRawBuffer().length);
        rawIntBuffer.put(state.getRawBuffer());
        vertexCount = state.getVertexCount();
        vertexFormat = new VertexFormat(state.getVertexFormat());

        if (state.stateQuadSprites != null) {
            if (quadSprites == null) {
                quadSprites = quadSpritesPrev;
            }

            if (quadSprites == null || quadSprites.length < getBufferQuadSize()) {
                quadSprites = new TextureAtlasSprite[getBufferQuadSize()];
            }

            TextureAtlasSprite[] atextureatlassprite = state.stateQuadSprites;
            System.arraycopy(atextureatlassprite, 0, quadSprites, 0, atextureatlassprite.length);
        } else {
            if (quadSprites != null) {
                quadSpritesPrev = quadSprites;
            }

            quadSprites = null;
        }
    }

    public int getBufferSize() {
        return vertexCount * vertexFormat.getIntegerSize();
    }

    public void reset() {
        vertexCount = 0;
        vertexFormatElement = null;
        vertexFormatIndex = 0;
        quadSprite = null;

        if (SmartAnimations.isActive()) {
            if (animatedSprites == null) {
                animatedSprites = animatedSpritesCached;
            }

            animatedSprites.clear();
        } else if (animatedSprites != null) {
            animatedSprites = null;
        }

        modeTriangles = false;
    }

    public void begin(int glMode, VertexFormat format) {
        if (isDrawing) {
            throw new IllegalStateException("Already building!");
        } else {
            isDrawing = true;
            reset();
            drawMode = glMode;
            vertexFormat = format;
            vertexFormatElement = format.getElement(vertexFormatIndex);
            noColor = false;
            byteBuffer.limit(byteBuffer.capacity());

            if (Config.isShaders()) {
                SVertexBuilder.endSetVertexFormat(this);
            }

            if (Config.isMultiTexture()) {
                if (blockLayer != null) {
                    if (quadSprites == null) {
                        quadSprites = quadSpritesPrev;
                    }

                    if (quadSprites == null || quadSprites.length < getBufferQuadSize()) {
                        quadSprites = new TextureAtlasSprite[getBufferQuadSize()];
                    }
                }
            } else {
                if (quadSprites != null) {
                    quadSpritesPrev = quadSprites;
                }

                quadSprites = null;
            }
        }
    }

    public WorldRenderer tex(double u, double v) {
        if (quadSprite != null && quadSprites != null) {
            u = quadSprite.toSingleU((float) u);
            v = quadSprite.toSingleV((float) v);
            quadSprites[vertexCount / 4] = quadSprite;
        }

        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

        switch (vertexFormatElement.getType()) {
            case FLOAT:
                byteBuffer.putFloat(i, (float) u);
                byteBuffer.putFloat(i + 4, (float) v);
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, (int) u);
                byteBuffer.putInt(i + 4, (int) v);
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short) ((int) v));
                byteBuffer.putShort(i + 2, (short) ((int) u));
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte) ((int) v));
                byteBuffer.put(i + 1, (byte) ((int) u));
        }

        nextVertexFormatIndex();
        return this;
    }

    public WorldRenderer lightmap(int p_181671_1_, int p_181671_2_) {
        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

        switch (vertexFormatElement.getType()) {
            case FLOAT:
                byteBuffer.putFloat(i, (float) p_181671_1_);
                byteBuffer.putFloat(i + 4, (float) p_181671_2_);
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, p_181671_1_);
                byteBuffer.putInt(i + 4, p_181671_2_);
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short) p_181671_2_);
                byteBuffer.putShort(i + 2, (short) p_181671_1_);
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte) p_181671_2_);
                byteBuffer.put(i + 1, (byte) p_181671_1_);
        }

        nextVertexFormatIndex();
        return this;
    }

    public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_) {
        int i = (vertexCount - 4) * vertexFormat.getIntegerSize() + vertexFormat.getUvOffsetById(1) / 4;
        int j = vertexFormat.getNextOffset() >> 2;
        rawIntBuffer.put(i, p_178962_1_);
        rawIntBuffer.put(i + j, p_178962_2_);
        rawIntBuffer.put(i + j * 2, p_178962_3_);
        rawIntBuffer.put(i + j * 3, p_178962_4_);
    }

    public void putPosition(double x, double y, double z) {
        int i = vertexFormat.getIntegerSize();
        int j = (vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k) {
            int l = j + k * i;
            int i1 = l + 1;
            int j1 = i1 + 1;
            rawIntBuffer.put(l, Float.floatToRawIntBits((float) (x + xOffset) + Float.intBitsToFloat(rawIntBuffer.get(l))));
            rawIntBuffer.put(i1, Float.floatToRawIntBits((float) (y + yOffset) + Float.intBitsToFloat(rawIntBuffer.get(i1))));
            rawIntBuffer.put(j1, Float.floatToRawIntBits((float) (z + zOffset) + Float.intBitsToFloat(rawIntBuffer.get(j1))));
        }
    }

    public int getColorIndex(int p_78909_1_) {
        return ((vertexCount - p_78909_1_) * vertexFormat.getNextOffset() + vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int p_178978_4_) {
        int i = getColorIndex(p_178978_4_);
        int j = -1;

        if (!noColor) {
            j = rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * red);
                int l = (int) ((float) (j >> 8 & 255) * green);
                int i1 = (int) ((float) (j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | i1 << 16 | l << 8 | k;
            } else {
                int j1 = (int) ((float) (j >> 24 & 255) * red);
                int k1 = (int) ((float) (j >> 16 & 255) * green);
                int l1 = (int) ((float) (j >> 8 & 255) * blue);
                j = j & 255;
                j = j | j1 << 24 | k1 << 16 | l1 << 8;
            }
        }

        rawIntBuffer.put(i, j);
    }

    private void putColor(int argb, int p_178988_2_) {
        int i = getColorIndex(p_178988_2_);
        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        int i1 = argb >> 24 & 255;
        putColorRGBA(i, j, k, l, i1);
    }

    public void putColorRGB_F(float red, float green, float blue, int p_178994_4_) {
        int i = getColorIndex(p_178994_4_);
        int j = MathHelper.clamp_int((int) (red * 255.0F), 0, 255);
        int k = MathHelper.clamp_int((int) (green * 255.0F), 0, 255);
        int l = MathHelper.clamp_int((int) (blue * 255.0F), 0, 255);
        putColorRGBA(i, j, k, l, 255);
    }

    public void putColorRGBA(int index, int red, int p_178972_3_, int p_178972_4_, int p_178972_5_) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            rawIntBuffer.put(index, p_178972_5_ << 24 | p_178972_4_ << 16 | p_178972_3_ << 8 | red);
        } else {
            rawIntBuffer.put(index, red << 24 | p_178972_3_ << 16 | p_178972_4_ << 8 | p_178972_5_);
        }
    }

    public void noColor() {
        noColor = true;
    }

    public WorldRenderer color(float red, float green, float blue, float alpha) {
        return color((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F));
    }

    public WorldRenderer color(int red, int green, int blue, int alpha) {
        if (noColor) {
            return this;
        } else {
            int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

            switch (vertexFormatElement.getType()) {
                case FLOAT:
                    byteBuffer.putFloat(i, (float) red / 255.0F);
                    byteBuffer.putFloat(i + 4, (float) green / 255.0F);
                    byteBuffer.putFloat(i + 8, (float) blue / 255.0F);
                    byteBuffer.putFloat(i + 12, (float) alpha / 255.0F);
                    break;

                case UINT:
                case INT:
                    byteBuffer.putFloat(i, (float) red);
                    byteBuffer.putFloat(i + 4, (float) green);
                    byteBuffer.putFloat(i + 8, (float) blue);
                    byteBuffer.putFloat(i + 12, (float) alpha);
                    break;

                case USHORT:
                case SHORT:
                    byteBuffer.putShort(i, (short) red);
                    byteBuffer.putShort(i + 2, (short) green);
                    byteBuffer.putShort(i + 4, (short) blue);
                    byteBuffer.putShort(i + 6, (short) alpha);
                    break;

                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        byteBuffer.put(i, (byte) red);
                        byteBuffer.put(i + 1, (byte) green);
                        byteBuffer.put(i + 2, (byte) blue);
                        byteBuffer.put(i + 3, (byte) alpha);
                    } else {
                        byteBuffer.put(i, (byte) alpha);
                        byteBuffer.put(i + 1, (byte) blue);
                        byteBuffer.put(i + 2, (byte) green);
                        byteBuffer.put(i + 3, (byte) red);
                    }
            }

            nextVertexFormatIndex();
            return this;
        }
    }

    public void addVertexData(int[] vertexData) {
        if (Config.isShaders()) {
            SVertexBuilder.beginAddVertexData(this, vertexData);
        }

        growBuffer(vertexData.length);
        rawIntBuffer.position(getBufferSize());
        rawIntBuffer.put(vertexData);
        vertexCount += vertexData.length / vertexFormat.getIntegerSize();

        if (Config.isShaders()) {
            SVertexBuilder.endAddVertexData(this);
        }
    }

    public void endVertex() {
        ++vertexCount;
        growBuffer(vertexFormat.getIntegerSize());
        vertexFormatIndex = 0;
        vertexFormatElement = vertexFormat.getElement(vertexFormatIndex);

        if (Config.isShaders()) {
            SVertexBuilder.endAddVertex(this);
        }
    }

    public WorldRenderer pos(double x, double y, double z) {
        if (Config.isShaders()) {
            SVertexBuilder.beginAddVertex(this);
        }

        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

        switch (vertexFormatElement.getType()) {
            case FLOAT:
                byteBuffer.putFloat(i, (float) (x + xOffset));
                byteBuffer.putFloat(i + 4, (float) (y + yOffset));
                byteBuffer.putFloat(i + 8, (float) (z + zOffset));
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, Float.floatToRawIntBits((float) (x + xOffset)));
                byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float) (y + yOffset)));
                byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float) (z + zOffset)));
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short) ((int) (x + xOffset)));
                byteBuffer.putShort(i + 2, (short) ((int) (y + yOffset)));
                byteBuffer.putShort(i + 4, (short) ((int) (z + zOffset)));
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte) ((int) (x + xOffset)));
                byteBuffer.put(i + 1, (byte) ((int) (y + yOffset)));
                byteBuffer.put(i + 2, (byte) ((int) (z + zOffset)));
        }

        nextVertexFormatIndex();
        return this;
    }

    public void putNormal(float x, float y, float z) {
        int i = (byte) ((int) (x * 127.0F)) & 255;
        int j = (byte) ((int) (y * 127.0F)) & 255;
        int k = (byte) ((int) (z * 127.0F)) & 255;
        int l = i | j << 8 | k << 16;
        int i1 = vertexFormat.getNextOffset() >> 2;
        int j1 = (vertexCount - 4) * i1 + vertexFormat.getNormalOffset() / 4;
        rawIntBuffer.put(j1, l);
        rawIntBuffer.put(j1 + i1, l);
        rawIntBuffer.put(j1 + i1 * 2, l);
        rawIntBuffer.put(j1 + i1 * 3, l);
    }

    private void nextVertexFormatIndex() {
        ++vertexFormatIndex;
        vertexFormatIndex %= vertexFormat.getElementCount();
        vertexFormatElement = vertexFormat.getElement(vertexFormatIndex);

        if (vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
            nextVertexFormatIndex();
        }
    }

    public WorldRenderer normal(float p_181663_1_, float p_181663_2_, float p_181663_3_) {
        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.getOffset(vertexFormatIndex);

        switch (vertexFormatElement.getType()) {
            case FLOAT:
                byteBuffer.putFloat(i, p_181663_1_);
                byteBuffer.putFloat(i + 4, p_181663_2_);
                byteBuffer.putFloat(i + 8, p_181663_3_);
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, (int) p_181663_1_);
                byteBuffer.putInt(i + 4, (int) p_181663_2_);
                byteBuffer.putInt(i + 8, (int) p_181663_3_);
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short) ((int) (p_181663_1_ * 32767.0F) & 65535));
                byteBuffer.putShort(i + 2, (short) ((int) (p_181663_2_ * 32767.0F) & 65535));
                byteBuffer.putShort(i + 4, (short) ((int) (p_181663_3_ * 32767.0F) & 65535));
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte) ((int) (p_181663_1_ * 127.0F) & 255));
                byteBuffer.put(i + 1, (byte) ((int) (p_181663_2_ * 127.0F) & 255));
                byteBuffer.put(i + 2, (byte) ((int) (p_181663_3_ * 127.0F) & 255));
        }

        nextVertexFormatIndex();
        return this;
    }

    public void setTranslation(double x, double y, double z) {
        xOffset = x;
        yOffset = y;
        zOffset = z;
    }

    public void finishDrawing() {
        if (!isDrawing) {
            throw new IllegalStateException("Not building!");
        } else {
            isDrawing = false;
            byteBuffer.position(0);
            byteBuffer.limit(getBufferSize() * 4);
        }
    }

    public ByteBuffer getByteBuffer() {
        return modeTriangles ? byteBufferTriangles : byteBuffer;
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    public int getVertexCount() {
        return modeTriangles ? vertexCount / 4 * 6 : vertexCount;
    }

    public int getDrawMode() {
        return modeTriangles ? 4 : drawMode;
    }

    public void putColor4(int argb) {
        for (int i = 0; i < 4; ++i) {
            putColor(argb, i + 1);
        }
    }

    public void putColorRGB_F4(float red, float green, float blue) {
        for (int i = 0; i < 4; ++i) {
            putColorRGB_F(red, green, blue, i + 1);
        }
    }

    public void putSprite(TextureAtlasSprite p_putSprite_1_) {
        if (animatedSprites != null && p_putSprite_1_ != null && p_putSprite_1_.getAnimationIndex() >= 0) {
            animatedSprites.set(p_putSprite_1_.getAnimationIndex());
        }

        if (quadSprites != null) {
            int i = vertexCount / 4;
            quadSprites[i - 1] = p_putSprite_1_;
        }
    }

    public void setSprite(TextureAtlasSprite p_setSprite_1_) {
        if (animatedSprites != null && p_setSprite_1_ != null && p_setSprite_1_.getAnimationIndex() >= 0) {
            animatedSprites.set(p_setSprite_1_.getAnimationIndex());
        }

        if (quadSprites != null) {
            quadSprite = p_setSprite_1_;
        }
    }

    public boolean isMultiTexture() {
        return quadSprites != null;
    }

    public void drawMultiTexture() {
        if (quadSprites != null) {
            int i = Config.getMinecraft().getTextureMapBlocks().getCountRegisteredSprites();

            if (drawnIcons.length <= i) {
                drawnIcons = new boolean[i + 1];
            }

            Arrays.fill(drawnIcons, false);
            int j = 0;
            int k = -1;
            int l = vertexCount / 4;

            for (int i1 = 0; i1 < l; ++i1) {
                TextureAtlasSprite textureatlassprite = quadSprites[i1];

                if (textureatlassprite != null) {
                    int j1 = textureatlassprite.getIndexInMap();

                    if (!drawnIcons[j1]) {
                        if (textureatlassprite == TextureUtils.iconGrassSideOverlay) {
                            if (k < 0) {
                                k = i1;
                            }
                        } else {
                            i1 = drawForIcon(textureatlassprite, i1) - 1;
                            ++j;

                            if (blockLayer != EnumWorldBlockLayer.TRANSLUCENT) {
                                drawnIcons[j1] = true;
                            }
                        }
                    }
                }
            }

            if (k >= 0) {
                drawForIcon(TextureUtils.iconGrassSideOverlay, k);
                ++j;
            }

            if (j > 0) {
            }
        }
    }

    private int drawForIcon(TextureAtlasSprite p_drawForIcon_1_, int p_drawForIcon_2_) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_drawForIcon_1_.glSpriteTextureId);
        int i = -1;
        int j = -1;
        int k = vertexCount / 4;

        for (int l = p_drawForIcon_2_; l < k; ++l) {
            TextureAtlasSprite textureatlassprite = quadSprites[l];

            if (textureatlassprite == p_drawForIcon_1_) {
                if (j < 0) {
                    j = l;
                }
            } else if (j >= 0) {
                draw(j, l);

                if (blockLayer == EnumWorldBlockLayer.TRANSLUCENT) {
                    return l;
                }

                j = -1;

                if (i < 0) {
                    i = l;
                }
            }
        }

        if (j >= 0) {
            draw(j, k);
        }

        if (i < 0) {
            i = k;
        }

        return i;
    }

    private void draw(int p_draw_1_, int p_draw_2_) {
        int i = p_draw_2_ - p_draw_1_;

        if (i > 0) {
            int j = p_draw_1_ * 4;
            int k = i * 4;
            GL11.glDrawArrays(drawMode, j, k);
        }
    }

    private int getBufferQuadSize() {
        return rawIntBuffer.capacity() * 4 / (vertexFormat.getIntegerSize() * 4);
    }

    public RenderEnv getRenderEnv(IBlockState p_getRenderEnv_1_, BlockPos p_getRenderEnv_2_) {
        if (renderEnv == null) {
            renderEnv = new RenderEnv(p_getRenderEnv_1_, p_getRenderEnv_2_);
            return renderEnv;
        } else {
            renderEnv.reset(p_getRenderEnv_1_, p_getRenderEnv_2_);
            return renderEnv;
        }
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public double getXOffset() {
        return xOffset;
    }

    public double getYOffset() {
        return yOffset;
    }

    public double getZOffset() {
        return zOffset;
    }

    public EnumWorldBlockLayer getBlockLayer() {
        return blockLayer;
    }

    public void setBlockLayer(EnumWorldBlockLayer p_setBlockLayer_1_) {
        blockLayer = p_setBlockLayer_1_;

        if (p_setBlockLayer_1_ == null) {
            if (quadSprites != null) {
                quadSpritesPrev = quadSprites;
            }

            quadSprites = null;
            quadSprite = null;
        }
    }

    public void putColorMultiplierRgba(float p_putColorMultiplierRgba_1_, float p_putColorMultiplierRgba_2_, float p_putColorMultiplierRgba_3_, float p_putColorMultiplierRgba_4_, int p_putColorMultiplierRgba_5_) {
        int i = getColorIndex(p_putColorMultiplierRgba_5_);
        int j = -1;

        if (!noColor) {
            j = rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * p_putColorMultiplierRgba_1_);
                int l = (int) ((float) (j >> 8 & 255) * p_putColorMultiplierRgba_2_);
                int i1 = (int) ((float) (j >> 16 & 255) * p_putColorMultiplierRgba_3_);
                int j1 = (int) ((float) (j >> 24 & 255) * p_putColorMultiplierRgba_4_);
                j = j1 << 24 | i1 << 16 | l << 8 | k;
            } else {
                int k1 = (int) ((float) (j >> 24 & 255) * p_putColorMultiplierRgba_1_);
                int l1 = (int) ((float) (j >> 16 & 255) * p_putColorMultiplierRgba_2_);
                int i2 = (int) ((float) (j >> 8 & 255) * p_putColorMultiplierRgba_3_);
                int j2 = (int) ((float) (j & 255) * p_putColorMultiplierRgba_4_);
                j = k1 << 24 | l1 << 16 | i2 << 8 | j2;
            }
        }

        rawIntBuffer.put(i, j);
    }

    public void quadsToTriangles() {
        if (drawMode == 7) {
            if (byteBufferTriangles == null) {
                byteBufferTriangles = GLAllocation.createDirectByteBuffer(byteBuffer.capacity() * 2);
            }

            if (byteBufferTriangles.capacity() < byteBuffer.capacity() * 2) {
                byteBufferTriangles = GLAllocation.createDirectByteBuffer(byteBuffer.capacity() * 2);
            }

            int i = vertexFormat.getNextOffset();
            int j = byteBuffer.limit();
            byteBuffer.rewind();
            byteBufferTriangles.clear();

            for (int k = 0; k < vertexCount; k += 4) {
                byteBuffer.limit((k + 3) * i);
                byteBuffer.position(k * i);
                byteBufferTriangles.put(byteBuffer);
                byteBuffer.limit((k + 1) * i);
                byteBuffer.position(k * i);
                byteBufferTriangles.put(byteBuffer);
                byteBuffer.limit((k + 2 + 2) * i);
                byteBuffer.position((k + 2) * i);
                byteBufferTriangles.put(byteBuffer);
            }

            byteBuffer.limit(j);
            byteBuffer.rewind();
            byteBufferTriangles.flip();
            modeTriangles = true;
        }
    }

    public boolean isColorDisabled() {
        return noColor;
    }

    public class State {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;
        private TextureAtlasSprite[] stateQuadSprites;

        public State(int[] p_i1_2_, VertexFormat p_i1_3_, TextureAtlasSprite[] p_i1_4_) {
            stateRawBuffer = p_i1_2_;
            stateVertexFormat = p_i1_3_;
            stateQuadSprites = p_i1_4_;
        }

        public State(int[] buffer, VertexFormat format) {
            stateRawBuffer = buffer;
            stateVertexFormat = format;
        }

        public int[] getRawBuffer() {
            return stateRawBuffer;
        }

        public int getVertexCount() {
            return stateRawBuffer.length / stateVertexFormat.getIntegerSize();
        }

        public VertexFormat getVertexFormat() {
            return stateVertexFormat;
        }
    }
}
