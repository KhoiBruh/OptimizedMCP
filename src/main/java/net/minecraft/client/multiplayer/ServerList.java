package net.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerList {
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<ServerData> servers = new ArrayList<>();

    public ServerList(Minecraft mcIn) {
        mc = mcIn;
        loadServerList();
    }

    public static void func_147414_b(ServerData p_147414_0_) {
        ServerList serverlist = new ServerList(Minecraft.getMinecraft());
        serverlist.loadServerList();

        for (int i = 0; i < serverlist.countServers(); ++i) {
            ServerData serverdata = serverlist.getServerData(i);

            if (serverdata.serverName.equals(p_147414_0_.serverName) && serverdata.serverIP.equals(p_147414_0_.serverIP)) {
                serverlist.func_147413_a(i, p_147414_0_);
                break;
            }
        }

        serverlist.saveServerList();
    }

    public void loadServerList() {
        try {
            servers.clear();
            NBTTagCompound nbttagcompound = CompressedStreamTools.read(new File(mc.mcDataDir, "servers.dat"));

            if (nbttagcompound == null) {
                return;
            }

            NBTTagList nbttaglist = nbttagcompound.getTagList("servers", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                servers.add(ServerData.getServerDataFromNBTCompound(nbttaglist.getCompoundTagAt(i)));
            }
        } catch (Exception exception) {
            logger.error("Couldn't load server list", exception);
        }
    }

    public void saveServerList() {
        try {
            NBTTagList nbttaglist = new NBTTagList();

            for (ServerData serverdata : servers) {
                nbttaglist.appendTag(serverdata.getNBTCompound());
            }

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("servers", nbttaglist);
            CompressedStreamTools.safeWrite(nbttagcompound, new File(mc.mcDataDir, "servers.dat"));
        } catch (Exception exception) {
            logger.error("Couldn't save server list", exception);
        }
    }

    public ServerData getServerData(int index) {
        return servers.get(index);
    }

    public void removeServerData(int index) {
        servers.remove(index);
    }

    public void addServerData(ServerData server) {
        servers.add(server);
    }

    public int countServers() {
        return servers.size();
    }

    public void swapServers(int p_78857_1_, int p_78857_2_) {
        ServerData serverdata = getServerData(p_78857_1_);
        servers.set(p_78857_1_, getServerData(p_78857_2_));
        servers.set(p_78857_2_, serverdata);
        saveServerList();
    }

    public void func_147413_a(int index, ServerData server) {
        servers.set(index, server);
    }
}
