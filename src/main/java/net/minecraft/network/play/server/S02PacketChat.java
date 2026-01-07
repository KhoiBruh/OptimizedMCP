package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

import java.io.IOException;

public class S02PacketChat implements Packet<INetHandlerPlayClient> {
    private IChatComponent chatComponent;
    private byte type;

    public S02PacketChat() {
    }

    public S02PacketChat(IChatComponent component) {
        this(component, (byte) 1);
    }

    public S02PacketChat(IChatComponent message, byte typeIn) {
        chatComponent = message;
        type = typeIn;
    }

    public void readPacketData(PacketBuffer buf) {
        chatComponent = buf.readChatComponent();
        type = buf.readByte();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeChatComponent(chatComponent);
        buf.writeByte(type);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleChat(this);
    }

    public IChatComponent getChatComponent() {
        return chatComponent;
    }

    public boolean isChat() {
        return type == 1 || type == 2;
    }

    public byte getType() {
        return type;
    }
}
