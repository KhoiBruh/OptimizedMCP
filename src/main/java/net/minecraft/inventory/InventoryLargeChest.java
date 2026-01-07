package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class InventoryLargeChest implements ILockableContainer {
    private final String name;
    private final ILockableContainer upperChest;
    private final ILockableContainer lowerChest;

    public InventoryLargeChest(String nameIn, ILockableContainer upperChestIn, ILockableContainer lowerChestIn) {
        name = nameIn;

        if (upperChestIn == null) {
            upperChestIn = lowerChestIn;
        }

        if (lowerChestIn == null) {
            lowerChestIn = upperChestIn;
        }

        upperChest = upperChestIn;
        lowerChest = lowerChestIn;

        if (upperChestIn.isLocked()) {
            lowerChestIn.setLockCode(upperChestIn.getLockCode());
        } else if (lowerChestIn.isLocked()) {
            upperChestIn.setLockCode(lowerChestIn.getLockCode());
        }
    }

    public int getSizeInventory() {
        return upperChest.getSizeInventory() + lowerChest.getSizeInventory();
    }

    public boolean isPartOfLargeChest(IInventory inventoryIn) {
        return upperChest == inventoryIn || lowerChest == inventoryIn;
    }

    public String getName() {
        return upperChest.hasCustomName() ? upperChest.getName() : (lowerChest.hasCustomName() ? lowerChest.getName() : name);
    }

    public boolean hasCustomName() {
        return upperChest.hasCustomName() || lowerChest.hasCustomName();
    }

    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
    }

    public ItemStack getStackInSlot(int index) {
        return index >= upperChest.getSizeInventory() ? lowerChest.getStackInSlot(index - upperChest.getSizeInventory()) : upperChest.getStackInSlot(index);
    }

    public ItemStack decrStackSize(int index, int count) {
        return index >= upperChest.getSizeInventory() ? lowerChest.decrStackSize(index - upperChest.getSizeInventory(), count) : upperChest.decrStackSize(index, count);
    }

    public ItemStack removeStackFromSlot(int index) {
        return index >= upperChest.getSizeInventory() ? lowerChest.removeStackFromSlot(index - upperChest.getSizeInventory()) : upperChest.removeStackFromSlot(index);
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= upperChest.getSizeInventory()) {
            lowerChest.setInventorySlotContents(index - upperChest.getSizeInventory(), stack);
        } else {
            upperChest.setInventorySlotContents(index, stack);
        }
    }

    public int getInventoryStackLimit() {
        return upperChest.getInventoryStackLimit();
    }

    public void markDirty() {
        upperChest.markDirty();
        lowerChest.markDirty();
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return upperChest.isUseableByPlayer(player) && lowerChest.isUseableByPlayer(player);
    }

    public void openInventory(EntityPlayer player) {
        upperChest.openInventory(player);
        lowerChest.openInventory(player);
    }

    public void closeInventory(EntityPlayer player) {
        upperChest.closeInventory(player);
        lowerChest.closeInventory(player);
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

    public boolean isLocked() {
        return upperChest.isLocked() || lowerChest.isLocked();
    }

    public LockCode getLockCode() {
        return upperChest.getLockCode();
    }

    public void setLockCode(LockCode code) {
        upperChest.setLockCode(code);
        lowerChest.setLockCode(code);
    }

    public String getGuiID() {
        return upperChest.getGuiID();
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    public void clear() {
        upperChest.clear();
        lowerChest.clear();
    }
}
