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

    public static String getVersion() {
        return VERSION;
    }

    public static void initialize() {
    }

    public static boolean openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
            return true;
        } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
            return false;
        }
    }
}
