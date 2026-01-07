package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C0FPacketConfirmTransaction implements Packet<INetHandlerPlayServer> {
    private int windowId;
    private short uid;
    private boolean accepted;

    public C0FPacketConfirmTransaction() {
    }

    public C0FPacketConfirmTransaction(int windowId, short uid, boolean accepted) {
        this.windowId = windowId;
        this.uid = uid;
        this.accepted = accepted;
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processConfirmTransaction(this);
    }

    public void readPacketData(PacketBuffer buf) {
        windowId = buf.readByte();
        uid = buf.readShort();
        accepted = buf.readByte() != 0;
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeByte(windowId);
        buf.writeShort(uid);
        buf.writeByte(accepted ? 1 : 0);
    }

    public int getWindowId() {
        return windowId;
    }

    public short getUid() {
        return uid;
    }
}
