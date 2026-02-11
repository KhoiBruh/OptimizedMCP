package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;

public class Main {

    private static class GameArgs {
        @Option(names = "--fullscreen")
        boolean fullscreen;

        @Option(names = "--checkGlErrors")
        boolean checkGlErrors;

        @Option(names = "--server")
        String server;

        @Option(names = "--port", defaultValue = "25565")
        int port;

        @Option(names = "--gameDir", defaultValue = ".")
        File gameDir;

        @Option(names = "--assetsDir")
        File assetsDir;

        @Option(names = "--resourcePackDir")
        File resourcePackDir;

        @Option(names = "--proxyHost")
        String proxyHost;

        @Option(names = "--proxyPort", defaultValue = "8080")
        int proxyPort;

        @Option(names = "--proxyUser")
        String proxyUser;

        @Option(names = "--proxyPass")
        String proxyPass;

        @Option(names = "--username")
        String username;

        @Option(names = "--uuid")
        String uuid;

        @Option(names = "--accessToken", required = true)
        String accessToken;

        @Option(names = "--version", required = true)
        String version;

        @Option(names = "--width", defaultValue = "854")
        int width;

        @Option(names = "--height", defaultValue = "480")
        int height;

        @Option(names = "--userProperties", defaultValue = "{}")
        String userProperties;

        @Option(names = "--profileProperties", defaultValue = "{}")
        String profileProperties;

        @Option(names = "--assetIndex")
        String assetIndex;

        @Option(names = "--userType", defaultValue = "legacy")
        String userType;

        @Unmatched
        List<String> unmatched;
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        GameArgs gameArgs = new GameArgs();
        gameArgs.username = "Player" + Minecraft.getSystemTime() % 1000L;
        new CommandLine(gameArgs).setStopAtPositional(true).parseArgs(args);

        if (gameArgs.unmatched != null && !gameArgs.unmatched.isEmpty())
            System.out.println("Completely ignored arguments: " + gameArgs.unmatched);

        String s = gameArgs.proxyHost;
        Proxy proxy = Proxy.NO_PROXY;

        if (s != null) {
            try {
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(s, gameArgs.proxyPort));
            } catch (Exception ignored) {
            }
        }

        final String proxyUser = gameArgs.proxyUser;
        final String proxyPass = gameArgs.proxyPass;

        if (!proxy.equals(Proxy.NO_PROXY) && isNullOrEmpty(proxyUser) && isNullOrEmpty(proxyPass)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                }
            });
        }

        int width = gameArgs.width;
        int height = gameArgs.height;

        boolean fullscreen = gameArgs.fullscreen;
        boolean checkGlErrors = gameArgs.checkGlErrors;

        String version = gameArgs.version;
        Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();

        PropertyMap user = gson.fromJson(gameArgs.userProperties, PropertyMap.class);
        PropertyMap profile = gson.fromJson(gameArgs.profileProperties, PropertyMap.class);

        File dir = gameArgs.gameDir;
        File assets = gameArgs.assetsDir != null ? gameArgs.assetsDir : new File(dir, "assets/");
        File resourcePacks = gameArgs.resourcePackDir != null ? gameArgs.resourcePackDir : new File(dir, "resourcepacks/");
        String id = gameArgs.uuid != null ? gameArgs.uuid : gameArgs.username;
        String assetIndex = gameArgs.assetIndex;
        String server = gameArgs.server;
        int port = gameArgs.port;
        Session session = new Session(gameArgs.username, id, gameArgs.accessToken, gameArgs.userType);

        GameConfiguration configuration = new GameConfiguration(
                new GameConfiguration.UserInformation(session, user, profile, proxy),
                new GameConfiguration.DisplayInformation(width, height, fullscreen, checkGlErrors),
                new GameConfiguration.FolderInformation(dir, resourcePacks, assets, assetIndex),
                new GameConfiguration.GameInformation(version),
                new GameConfiguration.ServerInformation(server, port)
        );

        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft.stopIntegratedServer();
            }
        });

        Thread.currentThread().setName("Client Thread");
        new Minecraft(configuration).run();
    }

    private static boolean isNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
