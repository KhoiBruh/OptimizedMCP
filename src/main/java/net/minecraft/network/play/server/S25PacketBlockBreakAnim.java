package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;

import java.io.IOException;

public class S25PacketBlockBreakAnim implements Packet<INetHandlerPlayClient> {
    private int breakerId;
    private BlockPos position;
    private int progress;

    public S25PacketBlockBreakAnim() {
    }

    public S25PacketBlockBreakAnim(int breakerId, BlockPos pos, int progress) {
        this.breakerId = breakerId;
        position = pos;
        this.progress = progress;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        breakerId = buf.readVarIntFromBuffer();
        position = buf.readBlockPos();
        progress = buf.readUnsignedByte();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(breakerId);
        buf.writeBlockPos(position);
        buf.writeByte(progress);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleBlockBreakAnim(this);
    }

    public int getBreakerId() {
        return breakerId;
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getProgress() {
        return progress;
    }
}
