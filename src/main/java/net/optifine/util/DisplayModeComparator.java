package net.optifine.util;

import org.lwjgl.opengl.DisplayMode;

import java.util.Comparator;

public class DisplayModeComparator implements Comparator<DisplayMode> {
    @Override
    public int compare(DisplayMode displaymode, DisplayMode other) {
        return
                displaymode.getWidth() != other.getWidth() ?
                        displaymode.getWidth() - other.getWidth() : (
                                displaymode.getHeight() != other.getHeight() ? displaymode.getHeight() - other.getHeight() : (
                                        displaymode.getBitsPerPixel() != other.getBitsPerPixel() ? displaymode.getBitsPerPixel() - other.getBitsPerPixel() : (
                                                displaymode.getFrequency() != other.getFrequency() ? displaymode.getFrequency() - other.getFrequency() : 0
                                        )
                                )
                        );
    }
}
