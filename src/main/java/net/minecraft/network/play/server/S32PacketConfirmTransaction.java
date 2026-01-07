package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S32PacketConfirmTransaction implements Packet<INetHandlerPlayClient> {
    private int windowId;
    private short actionNumber;
    private boolean field_148893_c;

    public S32PacketConfirmTransaction() {
    }

    public S32PacketConfirmTransaction(int windowIdIn, short actionNumberIn, boolean p_i45182_3_) {
        windowId = windowIdIn;
        actionNumber = actionNumberIn;
        field_148893_c = p_i45182_3_;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleConfirmTransaction(this);
    }

    public void readPacketData(PacketBuffer buf) {
        windowId = buf.readUnsignedByte();
        actionNumber = buf.readShort();
        field_148893_c = buf.readBoolean();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeByte(windowId);
        buf.writeShort(actionNumber);
        buf.writeBoolean(field_148893_c);
    }

    public int getWindowId() {
        return windowId;
    }

    public short getActionNumber() {
        return actionNumber;
    }

    public boolean func_148888_e() {
        return field_148893_c;
    }
}
