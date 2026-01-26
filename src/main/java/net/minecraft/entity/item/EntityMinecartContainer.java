package net.minecraft.entity.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;

import java.util.Arrays;

public abstract class EntityMinecartContainer extends EntityMinecart implements ILockableContainer {
    private ItemStack[] minecartContainerItems = new ItemStack[36];
    private boolean dropContentsWhenDead = true;

    public EntityMinecartContainer(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartContainer(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public void killMinecart(DamageSource source) {
        super.killMinecart(source);

        if (worldObj.getGameRules().getBoolean("doEntityDrops")) {
            InventoryHelper.dropInventoryItems(worldObj, this, this);
        }
    }

    public ItemStack getStackInSlot(int index) {
        return minecartContainerItems[index];
    }

    public ItemStack decrStackSize(int index, int count) {
        if (minecartContainerItems[index] != null) {
            if (minecartContainerItems[index].stackSize <= count) {
                ItemStack itemstack1 = minecartContainerItems[index];
                minecartContainerItems[index] = null;
                return itemstack1;
            } else {
                ItemStack itemstack = minecartContainerItems[index].splitStack(count);

                if (minecartContainerItems[index].stackSize == 0) {
                    minecartContainerItems[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        if (minecartContainerItems[index] != null) {
            ItemStack itemstack = minecartContainerItems[index];
            minecartContainerItems[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        minecartContainerItems[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    public void markDirty() {
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return !isDead && player.getDistanceSqToEntity(this) <= 64.0D;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public String getName() {
        return hasCustomName() ? getCustomNameTag() : "container.minecart";
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public void travelToDimension(int dimensionId) {
        dropContentsWhenDead = false;
        super.travelToDimension(dimensionId);
    }

    public void setDead() {
        if (dropContentsWhenDead) {
            InventoryHelper.dropInventoryItems(worldObj, this, this);
        }

        super.setDead();
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < minecartContainerItems.length; ++i) {
            if (minecartContainerItems[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                minecartContainerItems[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        tagCompound.setTag("Items", nbttaglist);
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        NBTTagList nbttaglist = tagCompund.getTagList("Items", 10);
        minecartContainerItems = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j < minecartContainerItems.length) {
                minecartContainerItems[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
    }

    public boolean interactFirst(EntityPlayer playerIn) {
        if (!worldObj.isRemote) {
            playerIn.displayGUIChest(this);
        }

        return true;
    }

    protected void applyDrag() {
        int i = 15 - Container.calcRedstoneFromInventory(this);
        float f = 0.98F + (float) i * 0.001F;
        motionX *= f;
        motionY *= 0.0D;
        motionZ *= f;
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
        return false;
    }

    public LockCode getLockCode() {
        return LockCode.EMPTY_CODE;
    }

    public void setLockCode(LockCode code) {
    }

    public void clear() {
        Arrays.fill(minecartContainerItems, null);
    }
}
