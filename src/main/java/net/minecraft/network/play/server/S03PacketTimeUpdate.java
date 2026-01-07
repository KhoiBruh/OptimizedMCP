package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S03PacketTimeUpdate implements Packet<INetHandlerPlayClient> {
    private long totalWorldTime;
    private long worldTime;

    public S03PacketTimeUpdate() {
    }

    public S03PacketTimeUpdate(long totalWorldTimeIn, long totalTimeIn, boolean doDayLightCycle) {
        totalWorldTime = totalWorldTimeIn;
        worldTime = totalTimeIn;

        if (!doDayLightCycle) {
            worldTime = -worldTime;

            if (worldTime == 0L) {
                worldTime = -1L;
            }
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        totalWorldTime = buf.readLong();
        worldTime = buf.readLong();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(totalWorldTime);
        buf.writeLong(worldTime);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleTimeUpdate(this);
    }

    public long getTotalWorldTime() {
        return totalWorldTime;
    }

    public long getWorldTime() {
        return worldTime;
    }
}
