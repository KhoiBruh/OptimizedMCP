package net.minecraft.network.play.client;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;

import java.io.IOException;

public class C08PacketPlayerBlockPlacement implements Packet<INetHandlerPlayServer> {
    private static final BlockPos field_179726_a = new BlockPos(-1, -1, -1);
    private BlockPos position;
    private int placedBlockDirection;
    private ItemStack stack;
    private float facingX;
    private float facingY;
    private float facingZ;

    public C08PacketPlayerBlockPlacement() {
    }

    public C08PacketPlayerBlockPlacement(ItemStack stackIn) {
        this(field_179726_a, 255, stackIn, 0.0F, 0.0F, 0.0F);
    }

    public C08PacketPlayerBlockPlacement(BlockPos positionIn, int placedBlockDirectionIn, ItemStack stackIn, float facingXIn, float facingYIn, float facingZIn) {
        position = positionIn;
        placedBlockDirection = placedBlockDirectionIn;
        stack = stackIn != null ? stackIn.copy() : null;
        facingX = facingXIn;
        facingY = facingYIn;
        facingZ = facingZIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        position = buf.readBlockPos();
        placedBlockDirection = buf.readUnsignedByte();
        stack = buf.readItemStackFromBuffer();
        facingX = (float) buf.readUnsignedByte() / 16.0F;
        facingY = (float) buf.readUnsignedByte() / 16.0F;
        facingZ = (float) buf.readUnsignedByte() / 16.0F;
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(position);
        buf.writeByte(placedBlockDirection);
        buf.writeItemStackToBuffer(stack);
        buf.writeByte((int) (facingX * 16.0F));
        buf.writeByte((int) (facingY * 16.0F));
        buf.writeByte((int) (facingZ * 16.0F));
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processPlayerBlockPlacement(this);
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getPlacedBlockDirection() {
        return placedBlockDirection;
    }

    public ItemStack getStack() {
        return stack;
    }

    public float getPlacedBlockOffsetX() {
        return facingX;
    }

    public float getPlacedBlockOffsetY() {
        return facingY;
    }

    public float getPlacedBlockOffsetZ() {
        return facingZ;
    }
}
