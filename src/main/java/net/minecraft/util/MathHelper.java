package net.minecraft.util;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.Random;
import java.util.UUID;

public class MathHelper {
    public static final float PI = roundToFloat(Math.PI);
    public static final float PId2 = roundToFloat(Math.PI / 2D);
    public static final float deg2Rad = roundToFloat(0.017453292519943295D);
    private static final float rad2Deg = roundToFloat(57.29577951308232D);
    private static final float radToIndex = roundToFloat(651.8986469044033D);

    private static final float[] SIN_TABLE_FAST = new float[4096];
    private static final float[] SIN_TABLE = new float[65536];
    private static final float[] ASIN_TABLE = new float[65536];
    private static final int[] multiplyDeBruijnBitPosition;
    public static boolean fastMath = false;

    static {
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
            ASIN_TABLE[i] = (float) Math.asin((double) i / 32767.5D - 1.0D);
        }

        for (int j = 0; j < SIN_TABLE_FAST.length; ++j) {
            SIN_TABLE_FAST[j] = roundToFloat(Math.sin((double) j * Math.PI * 2.0D / 4096.0D));
        }

        for (int j = -1; j < 2; ++j) {
            ASIN_TABLE[(int) (((double) j + 1.0D) * 32767.5D) & 65535] = (float) Math.asin(j);
        }

        multiplyDeBruijnBitPosition = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    }

    public static float sin(float value) {
        return fastMath ? SIN_TABLE_FAST[(int) (value * radToIndex) & 4095] : SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    public static float cos(float value) {
        return fastMath ? SIN_TABLE_FAST[(int) (value * radToIndex + 1024.0F) & 4095] : SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    public static float asin(float value) {
        return ASIN_TABLE[(int) ((double) (value + 1.0F) * 32767.5D) & 65535];
    }

    public static float acos(float value) {
        return PId2 - ASIN_TABLE[(int) ((double) (value + 1.0F) * 32767.5D) & 65535];
    }

    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    public static float toDeg(float angle) {
        return angle * rad2Deg;
    }

    public static float toRad(float angle) {
        return angle * deg2Rad;
    }

    public static float roundToFloat(double d) {
        return (float) (Math.round(d * 1.0E8D) / 1.0E8D);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sqrt(double value) {
        return (float) Math.sqrt(value);
    }

    public static float abs(float value) {
        return value >= 0 ? value : -value;
    }

    public static int abs_int(int value) {
        return value >= 0 ? value : -value;
    }

    public static double absMax(double a, double b) {
        if (a < 0) a = -a;
        if (b < 0) b = -b;

        return Math.max(a, b);
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static long floor_double_long(double value) {
        long i = (long) value;
        return value < i ? i - 1L : i;
    }

    public static int truncateDoubleToInt(double value) {
        return (int) (value + 1024) - 1024;
    }

    public static int ceil(float value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static int ceil(double value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static int roundUp(int p_154354_0_, int p_154354_1_) {
        if (p_154354_1_ == 0) {
            return 0;
        } else if (p_154354_0_ == 0) {
            return p_154354_1_;
        } else {
            if (p_154354_0_ < 0) {
                p_154354_1_ *= -1;
            }

            int i = p_154354_0_ % p_154354_1_;
            return i == 0 ? p_154354_0_ : p_154354_0_ + p_154354_1_ - i;
        }
    }

    public static int roundUpToPowerOfTwo(int value) {
        int i = value - 1;
        i = i | i >> 1;
        i = i | i >> 2;
        i = i | i >> 4;
        i = i | i >> 8;
        i = i | i >> 16;
        return i + 1;
    }

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double denormalizeClamp(double lowerBnd, double upperBnd, double slide) {
        return slide < 0.0D ? lowerBnd : (slide > 1.0D ? upperBnd : lowerBnd + (upperBnd - lowerBnd) * slide);
    }

    public static double func_181160_c(double p_181160_0_, double p_181160_2_, double p_181160_4_) {
        return (p_181160_0_ - p_181160_2_) / (p_181160_4_ - p_181160_2_);
    }

    public static double func_181162_h(double p_181162_0_) {
        return p_181162_0_ - Math.floor(p_181162_0_);
    }

    public static float wrapAngle(float value) {
        value = value % 360;

        if (value >= 180) value -= 360;
        if (value < -180) value += 360;

        return value;
    }

    public static double wrapAngle(double value) {
        value = value % 360;

        if (value >= 180) value -= 360;
        if (value < -180) value += 360;

        return value;
    }

    public static int normalizeAngle(int p_180184_0_, int p_180184_1_) {
        return (p_180184_0_ % p_180184_1_ + p_180184_1_) % p_180184_1_;
    }

    public static boolean epsilonEquals(float p_180185_0_, float p_180185_1_) {
        return abs(p_180185_1_ - p_180185_0_) < 1.0E-5F;
    }

    public static int avg(int[] vals) {
        if (vals.length != 0) {
            int i = sum(vals);
            return i / vals.length;
        } else return 0;
    }

    public static int sum(int[] vals) {
        if (vals.length != 0) {
            int i = 0;

            for (int k : vals) {
                i += k;
            }

            return i;
        } else return 0;
    }

    public static int bucketInt(int value, int base) {
        return value < 0 ? -((-value - 1) / base) - 1 : value / base;
    }

    private static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    private static int calculateLogBaseTwoDeBruijn(int value) {
        value = isPowerOfTwo(value) ? value : roundUpToPowerOfTwo(value);
        return multiplyDeBruijnBitPosition[(int) ((long) value * 125613361L >> 27) & 31];
    }

    public static int calculateLogBaseTwo(int value) {
        return calculateLogBaseTwoDeBruijn(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    public static int nextInt(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(min, max);
    }

    public static float nextFloat(Random random, float min, float max) {
        return min >= max ? min : random.nextFloat(min, max);
    }

    public static double nextDouble(Random random, double min, double max) {
        return min >= max ? min : random.nextDouble(min, max);
    }

    public static long getPositionRandom(Vec3i pos) {
        return getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ());
    }

    public static long getCoordinateRandom(int x, int y, int z) {
        long i = (x * 3129871L) ^ (long) z * 116129781L ^ (long) y;
        i = i * i * 42317861L + i * 11L;
        return i;
    }

    public static UUID getRandomUuid(Random rand) {
        long i = rand.nextLong() & -61441L | 16384L;
        long j = rand.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID(i, j);
    }

    public static int parse(String name, int defaultValue) {
        try {
            return Integer.parseInt(name);
        } catch (Throwable var3) {
            return defaultValue;
        }
    }

    public static int parseMax(String name, int defaultValue, int max) {
        return Math.max(max, parse(name, defaultValue));
    }

    public static double parse(String name, double defaultValue) {
        try {
            return Double.parseDouble(name);
        } catch (Throwable var4) {
            return defaultValue;
        }
    }

    public static double parseMax(String name, double defaultValue, double max) {
        return Math.max(max, parse(name, defaultValue));
    }

    public static int func_180183_b(float p_180183_0_, float p_180183_1_, float p_180183_2_) {
        return func_180181_b(
                floor(p_180183_0_ * 255),
                floor(p_180183_1_ * 255),
                floor(p_180183_2_ * 255)
        );
    }

    public static int func_180181_b(int p_180181_0_, int p_180181_1_, int p_180181_2_) {
        int i = (p_180181_0_ << 8) + p_180181_1_;
        i = (i << 8) + p_180181_2_;
        return i;
    }

    public static int func_180188_d(int p_180188_0_, int p_180188_1_) {
        int i = (p_180188_0_ & 16711680) >> 16;
        int j = (p_180188_1_ & 16711680) >> 16;
        int k = (p_180188_0_ & 65280) >> 8;
        int l = (p_180188_1_ & 65280) >> 8;
        int i1 = (p_180188_0_ & 255);
        int j1 = (p_180188_1_ & 255);
        int k1 = (int) ((float) i * (float) j / 255.0F);
        int l1 = (int) ((float) k * (float) l / 255.0F);
        int i2 = (int) ((float) i1 * (float) j1 / 255.0F);
        return p_180188_0_ & -16777216 | k1 << 16 | l1 << 8 | i2;
    }

    public static int hsvToRGB(float r, float g, float b) {
        int i = (int) (r * 6.0F) % 6;
        float f = r * 6.0F - i;
        float f1 = b * (1.0F - g);
        float f2 = b * (1.0F - f * g);
        float f3 = b * (1.0F - (1.0F - f) * g);
        float f4;
        float f5;
        float f6;

        switch (i) {
            case 0 -> {
                f4 = b;
                f5 = f3;
                f6 = f1;
            }
            case 1 -> {
                f4 = f2;
                f5 = b;
                f6 = f1;
            }
            case 2 -> {
                f4 = f1;
                f5 = b;
                f6 = f3;
            }
            case 3 -> {
                f4 = f1;
                f5 = f2;
                f6 = b;
            }
            case 4 -> {
                f4 = f3;
                f5 = f1;
                f6 = b;
            }
            case 5 -> {
                f4 = b;
                f5 = f1;
                f6 = f2;
            }
            default -> throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + r + ", " + g + ", " + b);
        }

        int j = clamp((int) (f4 * 255), 0, 255);
        int k = clamp((int) (f5 * 255), 0, 255);
        int l = clamp((int) (f6 * 255), 0, 255);
        return j << 16 | k << 8 | l;
    }

    public static void multiplyMat4xVec4(float[] vecOut, float[] matA, float[] vecB) {
        Matrix4f mat = new Matrix4f().set(matA);
        Vector4f vec = new Vector4f(vecB[0], vecB[1], vecB[2], vecB[3]);
        mat.transform(vec);
        vecOut[0] = vec.x;
        vecOut[1] = vec.y;
        vecOut[2] = vec.z;
        vecOut[3] = vec.w;
    }

    public static void invertMat4(float[] matOut, float[] m) {
        Matrix4f mat = new Matrix4f().set(m);
        mat.invert().get(matOut);
    }

    public static void invertMat4FBFA(FloatBuffer fbInvOut, FloatBuffer fbMatIn, float[] faInv, float[] faMat) {
        fbMatIn.get(faMat);
        invertMat4(faInv, faMat);
        fbInvOut.put(faInv);
    }
}
