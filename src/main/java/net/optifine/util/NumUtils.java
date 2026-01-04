package net.optifine.util;

public class NumUtils {
    public static float limit(float val, float min, float max) {
        return val < min ? min : (Math.min(val, max));
    }

}
