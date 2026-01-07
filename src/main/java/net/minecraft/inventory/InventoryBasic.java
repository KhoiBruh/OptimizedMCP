package net.minecraft.inventory;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class InventoryBasic implements IInventory {
    private String inventoryTitle;
    private final int slotsCount;
    private final ItemStack[] inventoryContents;
    private List<IInvBasic> changeListeners;
    private boolean hasCustomName;

    public InventoryBasic(String title, boolean customName, int slotCount) {
        inventoryTitle = title;
        hasCustomName = customName;
        slotsCount = slotCount;
        inventoryContents = new ItemStack[slotCount];
    }

    public InventoryBasic(IChatComponent title, int slotCount) {
        this(title.getUnformattedText(), true, slotCount);
    }

    public void addInventoryChangeListener(IInvBasic listener) {
        if (changeListeners == null) {
            changeListeners = Lists.newArrayList();
        }

        changeListeners.add(listener);
    }

    public void removeInventoryChangeListener(IInvBasic listener) {
        changeListeners.remove(listener);
    }

    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < inventoryContents.length ? inventoryContents[index] : null;
    }

    public ItemStack decrStackSize(int index, int count) {
        if (inventoryContents[index] != null) {
            if (inventoryContents[index].stackSize <= count) {
                ItemStack itemstack1 = inventoryContents[index];
                inventoryContents[index] = null;
                markDirty();
                return itemstack1;
            } else {
                ItemStack itemstack = inventoryContents[index].splitStack(count);

                if (inventoryContents[index].stackSize == 0) {
                    inventoryContents[index] = null;
                }

                markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack func_174894_a(ItemStack stack) {
        ItemStack itemstack = stack.copy();

        for (int i = 0; i < slotsCount; ++i) {
            ItemStack itemstack1 = getStackInSlot(i);

            if (itemstack1 == null) {
                setInventorySlotContents(i, itemstack);
                markDirty();
                return null;
            }

            if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
                int j = Math.min(getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.stackSize, j - itemstack1.stackSize);

                if (k > 0) {
                    itemstack1.stackSize += k;
                    itemstack.stackSize -= k;

                    if (itemstack.stackSize <= 0) {
                        markDirty();
                        return null;
                    }
                }
            }
        }

        if (itemstack.stackSize != stack.stackSize) {
            markDirty();
        }

        return itemstack;
    }

    public ItemStack removeStackFromSlot(int index) {
        if (inventoryContents[index] != null) {
            ItemStack itemstack = inventoryContents[index];
            inventoryContents[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryContents[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        markDirty();
    }

    public int getSizeInventory() {
        return slotsCount;
    }

    public String getName() {
        return inventoryTitle;
    }

    public boolean hasCustomName() {
        return hasCustomName;
    }

    public void setCustomName(String inventoryTitleIn) {
        hasCustomName = true;
        inventoryTitle = inventoryTitleIn;
    }

    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName(), new Object[0]);
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public void markDirty() {
        if (changeListeners != null) {
            for (IInvBasic changeListener : changeListeners) {
                changeListener.onInventoryChanged(this);
            }
        }
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
        for (int i = 0; i < inventoryContents.length; ++i) {
            inventoryContents[i] = null;
        }
    }
}
