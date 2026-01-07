package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class InventoryCrafting implements IInventory {
    private final ItemStack[] stackList;
    private final int inventoryWidth;
    private final int inventoryHeight;
    private final Container eventHandler;

    public InventoryCrafting(Container eventHandlerIn, int width, int height) {
        int i = width * height;
        stackList = new ItemStack[i];
        eventHandler = eventHandlerIn;
        inventoryWidth = width;
        inventoryHeight = height;
    }

    public int getSizeInventory() {
        return stackList.length;
    }

    public ItemStack getStackInSlot(int index) {
        return index >= getSizeInventory() ? null : stackList[index];
    }

    public ItemStack getStackInRowAndColumn(int row, int column) {
        return row >= 0 && row < inventoryWidth && column >= 0 && column <= inventoryHeight ? getStackInSlot(row + column * inventoryWidth) : null;
    }

    public String getName() {
        return "container.crafting";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName(), new Object[0]);
    }

    public ItemStack removeStackFromSlot(int index) {
        if (stackList[index] != null) {
            ItemStack itemstack = stackList[index];
            stackList[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public ItemStack decrStackSize(int index, int count) {
        if (stackList[index] != null) {
            if (stackList[index].stackSize <= count) {
                ItemStack itemstack1 = stackList[index];
                stackList[index] = null;
                eventHandler.onCraftMatrixChanged(this);
                return itemstack1;
            } else {
                ItemStack itemstack = stackList[index].splitStack(count);

                if (stackList[index].stackSize == 0) {
                    stackList[index] = null;
                }

                eventHandler.onCraftMatrixChanged(this);
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        stackList[index] = stack;
        eventHandler.onCraftMatrixChanged(this);
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
        for (int i = 0; i < stackList.length; ++i) {
            stackList[i] = null;
        }
    }

    public int getHeight() {
        return inventoryHeight;
    }

    public int getWidth() {
        return inventoryWidth;
    }
}
