package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S3APacketTabComplete implements Packet<INetHandlerPlayClient> {
    private String[] matches;

    public S3APacketTabComplete() {
    }

    public S3APacketTabComplete(String[] matchesIn) {
        matches = matchesIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        matches = new String[buf.readVarIntFromBuffer()];

        for (int i = 0; i < matches.length; ++i) {
            matches[i] = buf.readStringFromBuffer(32767);
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(matches.length);

        for (String s : matches) {
            buf.writeString(s);
        }
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleTabComplete(this);
    }

    public String[] func_149630_c() {
        return matches;
    }
}
