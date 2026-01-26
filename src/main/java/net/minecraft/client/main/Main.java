package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        OptionParser optionparser = new OptionParser();
        optionparser.allowsUnrecognizedOptions();
        optionparser.accepts("fullscreen");
        optionparser.accepts("checkGlErrors");

        OptionSpec<String> server = optionparser.accepts("server").withRequiredArg();
        OptionSpec<Integer> port = optionparser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(25565);
        OptionSpec<File> gameDir = optionparser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> assetsDir = optionparser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> resourcePackDir = optionparser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> proxyHost = optionparser.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> proxyPort = optionparser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", new String[0]).ofType(Integer.class);
        OptionSpec<String> proxyUser = optionparser.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> proxyPass = optionparser.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> username = optionparser.accepts("username").withRequiredArg().defaultsTo("Player" + Minecraft.getSystemTime() % 1000L);
        OptionSpec<String> uuid = optionparser.accepts("uuid").withRequiredArg();
        OptionSpec<String> accessToken = optionparser.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> version = optionparser.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> width = optionparser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> height = optionparser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<String> userProperties = optionparser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> profileProperties = optionparser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> assetIndex = optionparser.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> userType = optionparser.accepts("userType").withRequiredArg().defaultsTo("legacy");
        OptionSpec<String> nonOptions = optionparser.nonOptions();
        OptionSet optionset = optionparser.parse(args);
        List<String> list = optionset.valuesOf(nonOptions);

        if (!list.isEmpty()) System.out.println("Completely ignored arguments: " + list);

        String s = optionset.valueOf(proxyHost);
        Proxy proxy = Proxy.NO_PROXY;

        if (s != null) {
            try {
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(s, optionset.valueOf(proxyPort)));
            } catch (Exception var46) {
            }
        }

        final String s1 = optionset.valueOf(proxyUser);
        final String s2 = optionset.valueOf(proxyPass);

        if (!proxy.equals(Proxy.NO_PROXY) && isNullOrEmpty(s1) && isNullOrEmpty(s2)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(s1, s2.toCharArray());
                }
            });
        }

        int i = optionset.valueOf(width);
        int j = optionset.valueOf(height);
        boolean flag = optionset.has("fullscreen");
        boolean flag1 = optionset.has("checkGlErrors");
        String s3 = optionset.valueOf(version);
        Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap propertymap = gson.fromJson(optionset.valueOf(userProperties), PropertyMap.class);
        PropertyMap propertymap1 = gson.fromJson(optionset.valueOf(profileProperties), PropertyMap.class);
        File file1 = optionset.valueOf(gameDir);
        File file2 = optionset.has(assetsDir) ? optionset.valueOf(assetsDir) : new File(file1, "assets/");
        File file3 = optionset.has(resourcePackDir) ? optionset.valueOf(resourcePackDir) : new File(file1, "resourcepacks/");
        String s4 = optionset.has(uuid) ? uuid.value(optionset) : username.value(optionset);
        String s5 = optionset.has(assetIndex) ? assetIndex.value(optionset) : null;
        String s6 = optionset.valueOf(server);
        Integer integer = optionset.valueOf(port);
        Session session = new Session(username.value(optionset), s4, accessToken.value(optionset), userType.value(optionset));
        GameConfiguration gameconfiguration = new GameConfiguration(new GameConfiguration.UserInformation(session, propertymap, propertymap1, proxy), new GameConfiguration.DisplayInformation(i, j, flag, flag1), new GameConfiguration.FolderInformation(file1, file3, file2, s5), new GameConfiguration.GameInformation(s3), new GameConfiguration.ServerInformation(s6, integer));
        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft.stopIntegratedServer();
            }
        });
        Thread.currentThread().setName("Client thread");
        new Minecraft(gameconfiguration).run();
    }

    private static boolean isNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
