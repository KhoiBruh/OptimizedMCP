package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S0DPacketCollectItem implements Packet<INetHandlerPlayClient> {
    private int collectedItemEntityId;
    private int entityId;

    public S0DPacketCollectItem() {
    }

    public S0DPacketCollectItem(int collectedItemEntityIdIn, int entityIdIn) {
        collectedItemEntityId = collectedItemEntityIdIn;
        entityId = entityIdIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        collectedItemEntityId = buf.readVarIntFromBuffer();
        entityId = buf.readVarIntFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(collectedItemEntityId);
        buf.writeVarIntToBuffer(entityId);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleCollectItem(this);
    }

    public int getCollectedItemEntityID() {
        return collectedItemEntityId;
    }

    public int getEntityID() {
        return entityId;
    }
}
