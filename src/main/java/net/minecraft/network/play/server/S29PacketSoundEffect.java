package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.Validate;

public class S29PacketSoundEffect implements Packet<INetHandlerPlayClient> {
    private String soundName;
    private int posX;
    private int posY = Integer.MAX_VALUE;
    private int posZ;
    private float soundVolume;
    private int soundPitch;

    public S29PacketSoundEffect() {
    }

    public S29PacketSoundEffect(String soundNameIn, double soundX, double soundY, double soundZ, float volume, float pitch) {
        Validate.notNull(soundNameIn, "name");
        soundName = soundNameIn;
        posX = (int) (soundX * 8.0D);
        posY = (int) (soundY * 8.0D);
        posZ = (int) (soundZ * 8.0D);
        soundVolume = volume;
        soundPitch = (int) (pitch * 63.0F);
    }

    public void readPacketData(PacketBuffer buf) {
        soundName = buf.readStringFromBuffer(256);
        posX = buf.readInt();
        posY = buf.readInt();
        posZ = buf.readInt();
        soundVolume = buf.readFloat();
        soundPitch = buf.readUnsignedByte();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeString(soundName);
        buf.writeInt(posX);
        buf.writeInt(posY);
        buf.writeInt(posZ);
        buf.writeFloat(soundVolume);
        buf.writeByte(soundPitch);
    }

    public String getSoundName() {
        return soundName;
    }

    public double getX() {
        return (float) posX / 8.0F;
    }

    public double getY() {
        return (float) posY / 8.0F;
    }

    public double getZ() {
        return (float) posZ / 8.0F;
    }

    public float getVolume() {
        return soundVolume;
    }

    public float getPitch() {
        return (float) soundPitch / 63.0F;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSoundEffect(this);
    }
}
