package net.minecraft.network.handshake.client;

import net.minecraft.network.ConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;

public class C00Handshake implements Packet<INetHandlerHandshakeServer> {
    private int protocolVersion;
    private String ip;
    private int port;
    private ConnectionState requestedState;

    public C00Handshake() {
    }

    public C00Handshake(int version, String ip, int port, ConnectionState requestedState) {
        protocolVersion = version;
        this.ip = ip;
        this.port = port;
        this.requestedState = requestedState;
    }

    public void readPacketData(PacketBuffer buf) {
        protocolVersion = buf.readVarIntFromBuffer();
        ip = buf.readStringFromBuffer(255);
        port = buf.readUnsignedShort();
        requestedState = ConnectionState.getById(buf.readVarIntFromBuffer());
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(protocolVersion);
        buf.writeString(ip);
        buf.writeShort(port);
        buf.writeVarIntToBuffer(requestedState.getId());
    }

    public void processPacket(INetHandlerHandshakeServer handler) {
        handler.processHandshake(this);
    }

    public ConnectionState getRequestedState() {
        return requestedState;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }
}
