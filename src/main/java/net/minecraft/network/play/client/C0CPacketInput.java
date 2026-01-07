package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C0CPacketInput implements Packet<INetHandlerPlayServer> {
    private float strafeSpeed;
    private float forwardSpeed;
    private boolean jumping;
    private boolean sneaking;

    public C0CPacketInput() {
    }

    public C0CPacketInput(float strafeSpeed, float forwardSpeed, boolean jumping, boolean sneaking) {
        this.strafeSpeed = strafeSpeed;
        this.forwardSpeed = forwardSpeed;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    public void readPacketData(PacketBuffer buf) {
        strafeSpeed = buf.readFloat();
        forwardSpeed = buf.readFloat();
        byte b0 = buf.readByte();
        jumping = (b0 & 1) > 0;
        sneaking = (b0 & 2) > 0;
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeFloat(strafeSpeed);
        buf.writeFloat(forwardSpeed);
        byte b0 = 0;

        if (jumping) {
            b0 = (byte) (b0 | 1);
        }

        if (sneaking) {
            b0 = (byte) (b0 | 2);
        }

        buf.writeByte(b0);
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processInput(this);
    }

    public float getStrafeSpeed() {
        return strafeSpeed;
    }

    public float getForwardSpeed() {
        return forwardSpeed;
    }

    public boolean isJumping() {
        return jumping;
    }

    public boolean isSneaking() {
        return sneaking;
    }
}
