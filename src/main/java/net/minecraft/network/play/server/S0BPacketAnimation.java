package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S0BPacketAnimation implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private int type;

    public S0BPacketAnimation() {
    }

    public S0BPacketAnimation(Entity ent, int animationType) {
        entityId = ent.getEntityId();
        type = animationType;
    }

    public void readPacketData(PacketBuffer buf) {
        entityId = buf.readVarIntFromBuffer();
        type = buf.readUnsignedByte();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(type);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleAnimation(this);
    }

    public int getEntityID() {
        return entityId;
    }

    public int getAnimationType() {
        return type;
    }
}
