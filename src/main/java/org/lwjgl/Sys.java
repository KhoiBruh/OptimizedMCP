package org.lwjgl;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Sys {
    public static final String VERSION = Version.getVersion();
    private static final long timerOffset;

    static {
        timerOffset = System.nanoTime();
    }

    private Sys() {
    }

    public static long getTimerResolution() {
        return 1000000000;
    }

    public static long getTime() {
        return (System.nanoTime() - timerOffset) & 0x7FFFFFFFFFFFFFFFL;
    }

    public static void initialize() {
    }

    public static void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException | UnsupportedOperationException ignored) {
        }
    }
}
