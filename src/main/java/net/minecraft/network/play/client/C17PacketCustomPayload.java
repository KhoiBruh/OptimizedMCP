package net.minecraft.network.play.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C17PacketCustomPayload implements Packet<INetHandlerPlayServer> {
    private String channel;
    private PacketBuffer data;

    public C17PacketCustomPayload() {
    }

    public C17PacketCustomPayload(String channelIn, PacketBuffer dataIn) {
        channel = channelIn;
        data = dataIn;

        if (dataIn.writerIndex() > 32767) {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        channel = buf.readStringFromBuffer(20);
        int i = buf.readableBytes();

        if (i >= 0 && i <= 32767) {
            data = new PacketBuffer(buf.readBytes(i));
        } else {
            throw new IOException("Payload may not be larger than 32767 bytes");
        }
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeString(channel);
        buf.writeBytes(data);
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processVanilla250Packet(this);
    }

    public String getChannelName() {
        return channel;
    }

    public PacketBuffer getBufferData() {
        return data;
    }
}
