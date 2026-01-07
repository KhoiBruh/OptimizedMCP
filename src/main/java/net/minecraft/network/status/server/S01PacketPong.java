package net.minecraft.network.status.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.INetHandlerStatusClient;

public class S01PacketPong implements Packet<INetHandlerStatusClient> {
    private long clientTime;

    public S01PacketPong() {
    }

    public S01PacketPong(long time) {
        clientTime = time;
    }

    public void readPacketData(PacketBuffer buf) {
        clientTime = buf.readLong();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeLong(clientTime);
    }

    public void processPacket(INetHandlerStatusClient handler) {
        handler.handlePong(this);
    }
}
