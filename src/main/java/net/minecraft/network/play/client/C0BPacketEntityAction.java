package net.minecraft.network.play.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C0BPacketEntityAction implements Packet<INetHandlerPlayServer> {
    private int entityID;
    private C0BPacketEntityAction.Action action;
    private int auxData;

    public C0BPacketEntityAction() {
    }

    public C0BPacketEntityAction(Entity entity, C0BPacketEntityAction.Action action) {
        this(entity, action, 0);
    }

    public C0BPacketEntityAction(Entity entity, C0BPacketEntityAction.Action action, int auxData) {
        entityID = entity.getEntityId();
        this.action = action;
        this.auxData = auxData;
    }

    public void readPacketData(PacketBuffer buf) {
        entityID = buf.readVarIntFromBuffer();
        action = buf.readEnumValue(Action.class);
        auxData = buf.readVarIntFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(entityID);
        buf.writeEnumValue(action);
        buf.writeVarIntToBuffer(auxData);
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processEntityAction(this);
    }

    public C0BPacketEntityAction.Action getAction() {
        return action;
    }

    public int getAuxData() {
        return auxData;
    }

    public enum Action {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        RIDING_JUMP,
        OPEN_INVENTORY
    }
}
