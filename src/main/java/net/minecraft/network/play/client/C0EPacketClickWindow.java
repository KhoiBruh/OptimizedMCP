package net.minecraft.network.play.client;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C0EPacketClickWindow implements Packet<INetHandlerPlayServer> {
    private int windowId;
    private int slotId;
    private int usedButton;
    private short actionNumber;
    private ItemStack clickedItem;
    private int mode;

    public C0EPacketClickWindow() {
    }

    public C0EPacketClickWindow(int windowId, int slotId, int usedButton, int mode, ItemStack clickedItem, short actionNumber) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.usedButton = usedButton;
        this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
        this.actionNumber = actionNumber;
        this.mode = mode;
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processClickWindow(this);
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        windowId = buf.readByte();
        slotId = buf.readShort();
        usedButton = buf.readByte();
        actionNumber = buf.readShort();
        mode = buf.readByte();
        clickedItem = buf.readItemStackFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(windowId);
        buf.writeShort(slotId);
        buf.writeByte(usedButton);
        buf.writeShort(actionNumber);
        buf.writeByte(mode);
        buf.writeItemStackToBuffer(clickedItem);
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getUsedButton() {
        return usedButton;
    }

    public short getActionNumber() {
        return actionNumber;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }

    public int getMode() {
        return mode;
    }
}
