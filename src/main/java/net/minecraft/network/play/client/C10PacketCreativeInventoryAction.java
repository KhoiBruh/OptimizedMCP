package net.minecraft.network.play.client;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C10PacketCreativeInventoryAction implements Packet<INetHandlerPlayServer> {
    private int slotId;
    private ItemStack stack;

    public C10PacketCreativeInventoryAction() {
    }

    public C10PacketCreativeInventoryAction(int slotIdIn, ItemStack stackIn) {
        slotId = slotIdIn;
        stack = stackIn != null ? stackIn.copy() : null;
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processCreativeInventoryAction(this);
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        slotId = buf.readShort();
        stack = buf.readItemStackFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeShort(slotId);
        buf.writeItemStackToBuffer(stack);
    }

    public int getSlotId() {
        return slotId;
    }

    public ItemStack getStack() {
        return stack;
    }
}
