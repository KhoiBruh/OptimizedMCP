package net.minecraft.network.play.server;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S04PacketEntityEquipment implements Packet<INetHandlerPlayClient> {
    private int entityID;
    private int equipmentSlot;
    private ItemStack itemStack;

    public S04PacketEntityEquipment() {
    }

    public S04PacketEntityEquipment(int entityIDIn, int p_i45221_2_, ItemStack itemStackIn) {
        entityID = entityIDIn;
        equipmentSlot = p_i45221_2_;
        itemStack = itemStackIn == null ? null : itemStackIn.copy();
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityID = buf.readVarIntFromBuffer();
        equipmentSlot = buf.readShort();
        itemStack = buf.readItemStackFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityID);
        buf.writeShort(equipmentSlot);
        buf.writeItemStackToBuffer(itemStack);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityEquipment(this);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getEntityID() {
        return entityID;
    }

    public int getEquipmentSlot() {
        return equipmentSlot;
    }
}
