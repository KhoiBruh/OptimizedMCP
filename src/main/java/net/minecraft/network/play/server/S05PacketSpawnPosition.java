package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;

import java.io.IOException;

public class S05PacketSpawnPosition implements Packet<INetHandlerPlayClient> {
    private BlockPos spawnBlockPos;

    public S05PacketSpawnPosition() {
    }

    public S05PacketSpawnPosition(BlockPos spawnBlockPosIn) {
        spawnBlockPos = spawnBlockPosIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        spawnBlockPos = buf.readBlockPos();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(spawnBlockPos);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSpawnPosition(this);
    }

    public BlockPos getSpawnPos() {
        return spawnBlockPos;
    }
}
