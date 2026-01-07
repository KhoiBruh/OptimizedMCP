package net.minecraft.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Arrays;
import java.util.Random;

public class TileEntityDispenser extends TileEntityLockable implements IInventory {
    private static final Random RNG = new Random();
    protected String customName;
    private ItemStack[] stacks = new ItemStack[9];

    public int getSizeInventory() {
        return 9;
    }

    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    public ItemStack decrStackSize(int index, int count) {
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= count) {
                ItemStack itemstack1 = stacks[index];
                stacks[index] = null;
                markDirty();
                return itemstack1;
            } else {
                ItemStack itemstack = stacks[index].splitStack(count);

                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }

                markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        if (stacks[index] != null) {
            ItemStack itemstack = stacks[index];
            stacks[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public int getDispenseSlot() {
        int i = -1;
        int j = 1;

        for (int k = 0; k < stacks.length; ++k) {
            if (stacks[k] != null && RNG.nextInt(j++) == 0) {
                i = k;
            }
        }

        return i;
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        markDirty();
    }

    public int addItemStack(ItemStack stack) {
        for (int i = 0; i < stacks.length; ++i) {
            if (stacks[i] == null || stacks[i].getItem() == null) {
                setInventorySlotContents(i, stack);
                return i;
            }
        }

        return -1;
    }

    public String getName() {
        return hasCustomName() ? customName : "container.dispenser";
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        stacks = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < stacks.length) {
                stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        if (compound.hasKey("CustomName", 8)) {
            customName = compound.getString("CustomName");
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < stacks.length; ++i) {
            if (stacks[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                stacks[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);

        if (hasCustomName()) {
            compound.setString("CustomName", customName);
        }
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public String getGuiID() {
        return "minecraft:dispenser";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerDispenser(playerInventory, this);
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
        Arrays.fill(stacks, null);
    }
}
