package org.lwjgl.opengl;

public final class DisplayMode {
    private final int width, height, bpp, frequency;
    private final boolean fullscreen;

    public DisplayMode(int width, int height) {
        this(width, height, 0, 0, false);
    }

    DisplayMode(int width, int height, int bpp, int frequency) {
        this(width, height, bpp, frequency, false);
    }

    private DisplayMode(int width, int height, int bpp, int frequency, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        this.frequency = frequency;
        this.fullscreen = fullscreen;
    }

    public boolean isFullscreenCapable() {
        return fullscreen;
    }

    public int getBitsPerPixel() {
        return bpp;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean equals(Object obj) {
        if (obj instanceof DisplayMode dm)
            return dm.width == width && dm.height == height && dm.bpp == bpp && dm.frequency == frequency;

        return false;
    }

    public int hashCode() {
        return width ^ height ^ frequency ^ bpp;
    }

    public String toString() {
        return width + " x " + height + " x " + bpp + " @" + frequency + "Hz";
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBpp() {
        return bpp;
    }
}