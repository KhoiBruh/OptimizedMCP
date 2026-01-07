package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

import java.io.IOException;

public class S40PacketDisconnect implements Packet<INetHandlerPlayClient> {
    private IChatComponent reason;

    public S40PacketDisconnect() {
    }

    public S40PacketDisconnect(IChatComponent reasonIn) {
        reason = reasonIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        reason = buf.readChatComponent();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeChatComponent(reason);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleDisconnect(this);
    }

    public IChatComponent getReason() {
        return reason;
    }
}
