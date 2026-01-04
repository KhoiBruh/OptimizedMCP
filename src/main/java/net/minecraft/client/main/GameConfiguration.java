package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.util.Session;

import java.io.File;
import java.net.Proxy;

public record GameConfiguration(UserInformation userInfo, DisplayInformation displayInfo, FolderInformation folderInfo,
                                GameInformation gameInfo, ServerInformation serverInfo) {

    public record DisplayInformation(int width, int height, boolean fullscreen, boolean checkGlErrors) {
    }

    public record FolderInformation(File mcDataDir, File resourcePacksDir, File assetsDir, String assetIndex) {
    }

    public record GameInformation(String version) {
    }

    public record ServerInformation(String serverName, int serverPort) {
    }

    public record UserInformation(Session session, PropertyMap userProperties, PropertyMap profileProperties,
                                  Proxy proxy) {
    }
}
