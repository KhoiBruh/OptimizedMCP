package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S31PacketWindowProperty implements Packet<INetHandlerPlayClient> {
    private int windowId;
    private int varIndex;
    private int varValue;

    public S31PacketWindowProperty() {
    }

    public S31PacketWindowProperty(int windowIdIn, int varIndexIn, int varValueIn) {
        windowId = windowIdIn;
        varIndex = varIndexIn;
        varValue = varValueIn;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleWindowProperty(this);
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        windowId = buf.readUnsignedByte();
        varIndex = buf.readShort();
        varValue = buf.readShort();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(windowId);
        buf.writeShort(varIndex);
        buf.writeShort(varValue);
    }

    public int getWindowId() {
        return windowId;
    }

    public int getVarIndex() {
        return varIndex;
    }

    public int getVarValue() {
        return varValue;
    }
}
