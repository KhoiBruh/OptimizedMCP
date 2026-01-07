package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

import java.io.IOException;

public class S47PacketPlayerListHeaderFooter implements Packet<INetHandlerPlayClient> {
    private IChatComponent header;
    private IChatComponent footer;

    public S47PacketPlayerListHeaderFooter() {
    }

    public S47PacketPlayerListHeaderFooter(IChatComponent headerIn) {
        header = headerIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        header = buf.readChatComponent();
        footer = buf.readChatComponent();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeChatComponent(header);
        buf.writeChatComponent(footer);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handlePlayerListHeaderFooter(this);
    }

    public IChatComponent getHeader() {
        return header;
    }

    public IChatComponent getFooter() {
        return footer;
    }
}
