package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C16PacketClientStatus implements Packet<INetHandlerPlayServer> {
    private State status;

    public C16PacketClientStatus() {
    }

    public C16PacketClientStatus(State statusIn) {
        status = statusIn;
    }

    public void readPacketData(PacketBuffer buf) {
        status = buf.readEnumValue(State.class);
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeEnumValue(status);
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processClientStatus(this);
    }

    public State getStatus() {
        return status;
    }

    public enum State {
        PERFORM_RESPAWN,
        REQUEST_STATS,
        OPEN_INVENTORY_ACHIEVEMENT
    }
}
