package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class InventoryCraftResult implements IInventory {
    private final ItemStack[] stackResult = new ItemStack[1];

    public int getSizeInventory() {
        return 1;
    }

    public ItemStack getStackInSlot(int index) {
        return stackResult[0];
    }

    public String getName() {
        return "Result";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
    }

    public ItemStack decrStackSize(int index, int count) {
        if (stackResult[0] != null) {
            ItemStack itemstack = stackResult[0];
            stackResult[0] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        if (stackResult[0] != null) {
            ItemStack itemstack = stackResult[0];
            stackResult[0] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        stackResult[0] = stack;
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public void markDirty() {
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {
    }

    public int getFieldCount() {
        return 0;
    }

    public void clear() {
        for (int i = 0; i < stackResult.length; ++i) {
            stackResult[i] = null;
        }
    }
}
