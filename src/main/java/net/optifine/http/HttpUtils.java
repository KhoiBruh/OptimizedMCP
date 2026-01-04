package net.optifine.http;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Config;

import java.io.*;

public class HttpUtils {
    private static String playerItemsUrl = null;

    public static synchronized String getPlayerItemsUrl() {
        if (playerItemsUrl == null) {
            try {
                boolean flag = Config.parseBoolean(System.getProperty("player.models.local"), false);

                if (flag) {
                    File file1 = Minecraft.getMinecraft().mcDataDir;
                    File file2 = new File(file1, "playermodels");
                    playerItemsUrl = file2.toURI().toURL().toExternalForm();
                }
            } catch (Exception exception) {
                Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
            }

            if (playerItemsUrl == null) {
                playerItemsUrl = "http://s.optifine.net";
            }
        }

        return playerItemsUrl;
    }
}
