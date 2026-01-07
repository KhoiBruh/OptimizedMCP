package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

public class S19PacketEntityHeadLook implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private byte yaw;

    public S19PacketEntityHeadLook() {
    }

    public S19PacketEntityHeadLook(Entity entityIn, byte p_i45214_2_) {
        entityId = entityIn.getEntityId();
        yaw = p_i45214_2_;
    }

    public void readPacketData(PacketBuffer buf) {
        entityId = buf.readVarIntFromBuffer();
        yaw = buf.readByte();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(yaw);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityHeadLook(this);
    }

    public Entity getEntity(World worldIn) {
        return worldIn.getEntityByID(entityId);
    }

    public byte getYaw() {
        return yaw;
    }
}
