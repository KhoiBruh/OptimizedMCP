package net.minecraft.network.play.client;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C13PacketPlayerAbilities implements Packet<INetHandlerPlayServer> {
    private boolean invulnerable;
    private boolean flying;
    private boolean allowFlying;
    private boolean creativeMode;
    private float flySpeed;
    private float walkSpeed;

    public C13PacketPlayerAbilities() {
    }

    public C13PacketPlayerAbilities(PlayerCapabilities capabilities) {
        invulnerable = capabilities.disableDamage;
        flying = capabilities.isFlying;
        allowFlying = capabilities.allowFlying;
        creativeMode = capabilities.isCreativeMode;
        flySpeed = capabilities.getFlySpeed();
        walkSpeed = capabilities.getWalkSpeed();
    }

    public void readPacketData(PacketBuffer buf) {
        byte b0 = buf.readByte();
        invulnerable = (b0 & 1) > 0;
        flying = (b0 & 2) > 0;
        allowFlying = (b0 & 4) > 0;
        creativeMode = (b0 & 8) > 0;
        flySpeed = buf.readFloat();
        walkSpeed = buf.readFloat();
    }

    public void writePacketData(PacketBuffer buf) {
        byte b0 = 0;

        if (invulnerable) {
            b0 = (byte) (b0 | 1);
        }

        if (flying) {
            b0 = (byte) (b0 | 2);
        }

        if (allowFlying) {
            b0 = (byte) (b0 | 4);
        }

        if (creativeMode) {
            b0 = (byte) (b0 | 8);
        }

        buf.writeByte(b0);
        buf.writeFloat(flySpeed);
        buf.writeFloat(walkSpeed);
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processPlayerAbilities(this);
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean isInvulnerable) {
        invulnerable = isInvulnerable;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean isFlying) {
        flying = isFlying;
    }

    public boolean isAllowFlying() {
        return allowFlying;
    }

    public void setAllowFlying(boolean isAllowFlying) {
        allowFlying = isAllowFlying;
    }

    public boolean isCreativeMode() {
        return creativeMode;
    }

    public void setCreativeMode(boolean isCreativeMode) {
        creativeMode = isCreativeMode;
    }

    public void setFlySpeed(float flySpeedIn) {
        flySpeed = flySpeedIn;
    }

    public void setWalkSpeed(float walkSpeedIn) {
        walkSpeed = walkSpeedIn;
    }
}
