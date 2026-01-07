package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class Slot {
    public final IInventory inventory;
    private final int slotIndex;
    public int slotNumber;
    public int xDisplayPosition;
    public int yDisplayPosition;

    public Slot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        inventory = inventoryIn;
        slotIndex = index;
        xDisplayPosition = xPosition;
        yDisplayPosition = yPosition;
    }

    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        if (p_75220_1_ != null && p_75220_2_ != null) {
            if (p_75220_1_.getItem() == p_75220_2_.getItem()) {
                int i = p_75220_2_.stackSize - p_75220_1_.stackSize;

                if (i > 0) {
                    onCrafting(p_75220_1_, i);
                }
            }
        }
    }

    protected void onCrafting(ItemStack stack, int amount) {
    }

    protected void onCrafting(ItemStack stack) {
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
        onSlotChanged();
    }

    public boolean isItemValid(ItemStack stack) {
        return true;
    }

    public ItemStack getStack() {
        return inventory.getStackInSlot(slotIndex);
    }

    public boolean getHasStack() {
        return getStack() != null;
    }

    public void putStack(ItemStack stack) {
        inventory.setInventorySlotContents(slotIndex, stack);
        onSlotChanged();
    }

    public void onSlotChanged() {
        inventory.markDirty();
    }

    public int getSlotStackLimit() {
        return inventory.getInventoryStackLimit();
    }

    public int getItemStackLimit(ItemStack stack) {
        return getSlotStackLimit();
    }

    public String getSlotTexture() {
        return null;
    }

    public ItemStack decrStackSize(int amount) {
        return inventory.decrStackSize(slotIndex, amount);
    }

    public boolean isHere(IInventory inv, int slotIn) {
        return inv == inventory && slotIn == slotIndex;
    }

    public boolean canTakeStack(EntityPlayer playerIn) {
        return true;
    }

    public boolean canBeHovered() {
        return true;
    }
}
