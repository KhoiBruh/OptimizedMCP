package net.minecraft.network.play.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.io.IOException;

public class C02PacketUseEntity implements Packet<INetHandlerPlayServer> {
    private int entityId;
    private C02PacketUseEntity.Action action;
    private Vec3 hitVec;

    public C02PacketUseEntity() {
    }

    public C02PacketUseEntity(Entity entity, C02PacketUseEntity.Action action) {
        entityId = entity.getEntityId();
        this.action = action;
    }

    public C02PacketUseEntity(Entity entity, Vec3 hitVec) {
        this(entity, C02PacketUseEntity.Action.INTERACT_AT);
        this.hitVec = hitVec;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityId = buf.readVarIntFromBuffer();
        action = buf.readEnumValue(Action.class);

        if (action == C02PacketUseEntity.Action.INTERACT_AT) {
            hitVec = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityId);
        buf.writeEnumValue(action);

        if (action == C02PacketUseEntity.Action.INTERACT_AT) {
            buf.writeFloat((float) hitVec.xCoord());
            buf.writeFloat((float) hitVec.yCoord());
            buf.writeFloat((float) hitVec.zCoord());
        }
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processUseEntity(this);
    }

    public Entity getEntityFromWorld(World worldIn) {
        return worldIn.getEntityByID(entityId);
    }

    public C02PacketUseEntity.Action getAction() {
        return action;
    }

    public Vec3 getHitVec() {
        return hitVec;
    }

    public enum Action {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
