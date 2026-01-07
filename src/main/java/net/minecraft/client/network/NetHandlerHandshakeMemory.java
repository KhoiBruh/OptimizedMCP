package net.minecraft.client.network;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.IChatComponent;

public class NetHandlerHandshakeMemory implements INetHandlerHandshakeServer {
    private final MinecraftServer mcServer;
    private final NetworkManager networkManager;

    public NetHandlerHandshakeMemory(MinecraftServer mcServerIn, NetworkManager networkManagerIn) {
        mcServer = mcServerIn;
        networkManager = networkManagerIn;
    }

    public void processHandshake(C00Handshake packetIn) {
        networkManager.setConnectionState(packetIn.getRequestedState());
        networkManager.setNetHandler(new NetHandlerLoginServer(mcServer, networkManager));
    }

    public void onDisconnect(IChatComponent reason) {
    }
}
