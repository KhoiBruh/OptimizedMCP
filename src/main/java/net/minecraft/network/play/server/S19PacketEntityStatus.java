package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

import java.io.IOException;

public class S19PacketEntityStatus implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private byte logicOpcode;

    public S19PacketEntityStatus() {
    }

    public S19PacketEntityStatus(Entity entityIn, byte opCodeIn) {
        entityId = entityIn.getEntityId();
        logicOpcode = opCodeIn;
    }

    public void readPacketData(PacketBuffer buf) {
        entityId = buf.readInt();
        logicOpcode = buf.readByte();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeByte(logicOpcode);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityStatus(this);
    }

    public Entity getEntity(World worldIn) {
        return worldIn.getEntityByID(entityId);
    }

    public byte getOpCode() {
        return logicOpcode;
    }
}
