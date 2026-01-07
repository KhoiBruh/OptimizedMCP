package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class C07PacketPlayerDigging implements Packet<INetHandlerPlayServer> {
    private BlockPos position;
    private EnumFacing facing;
    private C07PacketPlayerDigging.Action status;

    public C07PacketPlayerDigging() {
    }

    public C07PacketPlayerDigging(C07PacketPlayerDigging.Action statusIn, BlockPos posIn, EnumFacing facingIn) {
        status = statusIn;
        position = posIn;
        facing = facingIn;
    }

    public void readPacketData(PacketBuffer buf) {
        status = buf.readEnumValue(Action.class);
        position = buf.readBlockPos();
        facing = EnumFacing.getFront(buf.readUnsignedByte());
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeEnumValue(status);
        buf.writeBlockPos(position);
        buf.writeByte(facing.getIndex());
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processPlayerDigging(this);
    }

    public BlockPos getPosition() {
        return position;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public C07PacketPlayerDigging.Action getStatus() {
        return status;
    }

    public enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM
    }
}
