package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.EnumParticleTypes;

import java.io.IOException;

public class S2APacketParticles implements Packet<INetHandlerPlayClient> {
    private EnumParticleTypes particleType;
    private float xCoord;
    private float yCoord;
    private float zCoord;
    private float xOffset;
    private float yOffset;
    private float zOffset;
    private float particleSpeed;
    private int particleCount;
    private boolean longDistance;
    private int[] particleArguments;

    public S2APacketParticles() {
    }

    public S2APacketParticles(EnumParticleTypes particleTypeIn, boolean longDistanceIn, float x, float y, float z, float xOffsetIn, float yOffset, float zOffset, float particleSpeedIn, int particleCountIn, int... particleArgumentsIn) {
        particleType = particleTypeIn;
        longDistance = longDistanceIn;
        xCoord = x;
        yCoord = y;
        zCoord = z;
        xOffset = xOffsetIn;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        particleSpeed = particleSpeedIn;
        particleCount = particleCountIn;
        particleArguments = particleArgumentsIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        particleType = EnumParticleTypes.getParticleFromId(buf.readInt());

        if (particleType == null) {
            particleType = EnumParticleTypes.BARRIER;
        }

        longDistance = buf.readBoolean();
        xCoord = buf.readFloat();
        yCoord = buf.readFloat();
        zCoord = buf.readFloat();
        xOffset = buf.readFloat();
        yOffset = buf.readFloat();
        zOffset = buf.readFloat();
        particleSpeed = buf.readFloat();
        particleCount = buf.readInt();
        int i = particleType.getArgumentCount();
        particleArguments = new int[i];

        for (int j = 0; j < i; ++j) {
            particleArguments[j] = buf.readVarIntFromBuffer();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(particleType.getParticleID());
        buf.writeBoolean(longDistance);
        buf.writeFloat(xCoord);
        buf.writeFloat(yCoord);
        buf.writeFloat(zCoord);
        buf.writeFloat(xOffset);
        buf.writeFloat(yOffset);
        buf.writeFloat(zOffset);
        buf.writeFloat(particleSpeed);
        buf.writeInt(particleCount);
        int i = particleType.getArgumentCount();

        for (int j = 0; j < i; ++j) {
            buf.writeVarIntToBuffer(particleArguments[j]);
        }
    }

    public EnumParticleTypes getParticleType() {
        return particleType;
    }

    public boolean isLongDistance() {
        return longDistance;
    }

    public double getXCoordinate() {
        return xCoord;
    }

    public double getYCoordinate() {
        return yCoord;
    }

    public double getZCoordinate() {
        return zCoord;
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public float getZOffset() {
        return zOffset;
    }

    public float getParticleSpeed() {
        return particleSpeed;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public int[] getParticleArgs() {
        return particleArguments;
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleParticles(this);
    }
}
