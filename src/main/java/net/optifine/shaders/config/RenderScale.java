package net.optifine.shaders.config;

public class RenderScale {
    private final float scale;
    private final float offsetX;
    private final float offsetY;

    public RenderScale(float scale, float offsetX, float offsetY) {
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float getScale() {
        return scale;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public String toString() {
        return scale + ", " + offsetX + ", " + offsetY;
    }
}
