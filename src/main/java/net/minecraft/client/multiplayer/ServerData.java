package net.minecraft.client.multiplayer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class ServerData {
    public String serverName;
    public String serverIP;
    public String populationInfo;
    public String serverMOTD;
    public long pingToServer;
    public int version = 47;
    public String gameVersion = "1.8.9";
    public boolean field_78841_f;
    public String playerList;
    private ServerData.ServerResourceMode resourceMode = ServerData.ServerResourceMode.PROMPT;
    private String serverIcon;
    private boolean lanServer;

    public ServerData(String name, String ip, boolean isLan) {
        serverName = name;
        serverIP = ip;
        lanServer = isLan;
    }

    public static ServerData getServerDataFromNBTCompound(NBTTagCompound nbtCompound) {
        ServerData serverdata = new ServerData(nbtCompound.getString("name"), nbtCompound.getString("ip"), false);

        if (nbtCompound.hasKey("icon", 8)) {
            serverdata.setBase64EncodedIconData(nbtCompound.getString("icon"));
        }

        if (nbtCompound.hasKey("acceptTextures", 1)) {
            if (nbtCompound.getBoolean("acceptTextures")) {
                serverdata.setResourceMode(ServerData.ServerResourceMode.ENABLED);
            } else {
                serverdata.setResourceMode(ServerData.ServerResourceMode.DISABLED);
            }
        } else {
            serverdata.setResourceMode(ServerData.ServerResourceMode.PROMPT);
        }

        return serverdata;
    }

    public NBTTagCompound getNBTCompound() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("name", serverName);
        nbttagcompound.setString("ip", serverIP);

        if (serverIcon != null) {
            nbttagcompound.setString("icon", serverIcon);
        }

        if (resourceMode == ServerData.ServerResourceMode.ENABLED) {
            nbttagcompound.setBoolean("acceptTextures", true);
        } else if (resourceMode == ServerData.ServerResourceMode.DISABLED) {
            nbttagcompound.setBoolean("acceptTextures", false);
        }

        return nbttagcompound;
    }

    public ServerData.ServerResourceMode getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(ServerData.ServerResourceMode mode) {
        resourceMode = mode;
    }

    public String getBase64EncodedIconData() {
        return serverIcon;
    }

    public void setBase64EncodedIconData(String icon) {
        serverIcon = icon;
    }

    public boolean isOnLAN() {
        return lanServer;
    }

    public void copyFrom(ServerData serverDataIn) {
        serverIP = serverDataIn.serverIP;
        serverName = serverDataIn.serverName;
        setResourceMode(serverDataIn.getResourceMode());
        serverIcon = serverDataIn.serverIcon;
        lanServer = serverDataIn.lanServer;
    }

    public enum ServerResourceMode {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final IChatComponent motd;

        ServerResourceMode(String name) {
            motd = new ChatComponentTranslation("addServer.resourcePack." + name);
        }

        public IChatComponent getMotd() {
            return motd;
        }
    }
}
