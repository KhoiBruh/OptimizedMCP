package net.optifine.config;

public class RangeInt {
    private final int min;
    private final int max;

    public RangeInt(int min, int max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public boolean isInRange(int val) {
        return val >= min && val <= max;
    }

    public int getMin() {
        return min;
    }

    public String toString() {
        return "min: " + min + ", max: " + max;
    }
}
