package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.PotionEffect;

import java.io.IOException;

public class S1EPacketRemoveEntityEffect implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private int effectId;

    public S1EPacketRemoveEntityEffect() {
    }

    public S1EPacketRemoveEntityEffect(int entityIdIn, PotionEffect effect) {
        entityId = entityIdIn;
        effectId = effect.getPotionID();
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityId = buf.readVarIntFromBuffer();
        effectId = buf.readUnsignedByte();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(effectId);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleRemoveEntityEffect(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getEffectId() {
        return effectId;
    }
}
