package net.minecraft.network.play.server;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;

import java.io.IOException;

public class S35PacketUpdateTileEntity implements Packet<INetHandlerPlayClient> {
    private BlockPos blockPos;
    private int metadata;
    private NBTTagCompound nbt;

    public S35PacketUpdateTileEntity() {
    }

    public S35PacketUpdateTileEntity(BlockPos blockPosIn, int metadataIn, NBTTagCompound nbtIn) {
        blockPos = blockPosIn;
        metadata = metadataIn;
        nbt = nbtIn;
    }

    public void readPacketData(PacketBuffer buf) {
        blockPos = buf.readBlockPos();
        metadata = buf.readUnsignedByte();
        nbt = buf.readNBTTagCompoundFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(blockPos);
        buf.writeByte((byte) metadata);
        buf.writeNBTTagCompoundToBuffer(nbt);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleUpdateTileEntity(this);
    }

    public BlockPos getPos() {
        return blockPos;
    }

    public int getTileEntityType() {
        return metadata;
    }

    public NBTTagCompound getNbtCompound() {
        return nbt;
    }
}
