package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S13PacketDestroyEntities implements Packet<INetHandlerPlayClient> {
    private int[] entityIDs;

    public S13PacketDestroyEntities() {
    }

    public S13PacketDestroyEntities(int... entityIDsIn) {
        entityIDs = entityIDsIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityIDs = new int[buf.readVarIntFromBuffer()];

        for (int i = 0; i < entityIDs.length; ++i) {
            entityIDs[i] = buf.readVarIntFromBuffer();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityIDs.length);

        for (int entityID : entityIDs) {
            buf.writeVarIntToBuffer(entityID);
        }
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleDestroyEntities(this);
    }

    public int[] getEntityIDs() {
        return entityIDs;
    }
}
