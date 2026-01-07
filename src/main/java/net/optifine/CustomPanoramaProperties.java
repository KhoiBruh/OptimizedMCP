package net.optifine;

import net.minecraft.util.ResourceLocation;
import net.optifine.config.ConnectedParser;

import java.util.Properties;

public class CustomPanoramaProperties {
    private final String path;
    private final ResourceLocation[] panoramaLocations;
    private final int weight;
    private final int blur1;
    private final int blur2;
    private final int blur3;
    private final int overlay1Top;
    private final int overlay1Bottom;
    private final int overlay2Top;
    private final int overlay2Bottom;

    public CustomPanoramaProperties(String path, Properties props) {
        ConnectedParser connectedparser = new ConnectedParser("CustomPanorama");
        this.path = path;
        panoramaLocations = new ResourceLocation[6];

        for (int i = 0; i < panoramaLocations.length; ++i) {
            panoramaLocations[i] = new ResourceLocation(path + "/panorama_" + i + ".png");
        }

        weight = connectedparser.parseInt(props.getProperty("weight"), 1);
        blur1 = connectedparser.parseInt(props.getProperty("blur1"), 64);
        blur2 = connectedparser.parseInt(props.getProperty("blur2"), 3);
        blur3 = connectedparser.parseInt(props.getProperty("blur3"), 3);
        overlay1Top = ConnectedParser.parseColor4(props.getProperty("overlay1.top"), -2130706433);
        overlay1Bottom = ConnectedParser.parseColor4(props.getProperty("overlay1.bottom"), 16777215);
        overlay2Top = ConnectedParser.parseColor4(props.getProperty("overlay2.top"), 0);
        overlay2Bottom = ConnectedParser.parseColor4(props.getProperty("overlay2.bottom"), Integer.MIN_VALUE);
    }

    public ResourceLocation[] getPanoramaLocations() {
        return panoramaLocations;
    }

    public int getBlur1() {
        return blur1;
    }

    public int getBlur2() {
        return blur2;
    }

    public int getBlur3() {
        return blur3;
    }

    public int getOverlay1Top() {
        return overlay1Top;
    }

    public int getOverlay1Bottom() {
        return overlay1Bottom;
    }

    public int getOverlay2Top() {
        return overlay2Top;
    }

    public int getOverlay2Bottom() {
        return overlay2Bottom;
    }

    public String toString() {
        return path + ", weight: " + weight + ", blur: " + blur1 + " " + blur2 + " " + blur3 + ", overlay: " + overlay1Top + " " + overlay1Bottom + " " + overlay2Top + " " + overlay2Bottom;
    }
}
