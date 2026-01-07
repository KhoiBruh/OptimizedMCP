package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.PotionEffect;

import java.io.IOException;

public class S1DPacketEntityEffect implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private byte effectId;
    private byte amplifier;
    private int duration;
    private byte hideParticles;

    public S1DPacketEntityEffect() {
    }

    public S1DPacketEntityEffect(int entityIdIn, PotionEffect effect) {
        entityId = entityIdIn;
        effectId = (byte) (effect.getPotionID() & 255);
        amplifier = (byte) (effect.getAmplifier() & 255);

        if (effect.getDuration() > 32767) {
            duration = 32767;
        } else {
            duration = effect.getDuration();
        }

        hideParticles = (byte) (effect.getIsShowParticles() ? 1 : 0);
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityId = buf.readVarIntFromBuffer();
        effectId = buf.readByte();
        amplifier = buf.readByte();
        duration = buf.readVarIntFromBuffer();
        hideParticles = buf.readByte();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(effectId);
        buf.writeByte(amplifier);
        buf.writeVarIntToBuffer(duration);
        buf.writeByte(hideParticles);
    }

    public boolean func_149429_c() {
        return duration == 32767;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityEffect(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public byte getEffectId() {
        return effectId;
    }

    public byte getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

    public boolean func_179707_f() {
        return hideParticles != 0;
    }
}
