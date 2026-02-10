package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class TileEntityHopper extends TileEntityLockable implements IHopper, ITickable {
    private ItemStack[] inventory = new ItemStack[5];
    private String customName;
    private int transferCooldown = -1;

    private static boolean isInventoryEmpty(IInventory inventoryIn, Direction side) {
        if (inventoryIn instanceof ISidedInventory isidedinventory) {
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int j : aint) {
                if (isidedinventory.getStackInSlot(j) != null) {
                    return false;
                }
            }
        } else {
            int j = inventoryIn.getSizeInventory();

            for (int k = 0; k < j; ++k) {
                if (inventoryIn.getStackInSlot(k) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean captureDroppedItems(IHopper p_145891_0_) {
        IInventory iinventory = getHopperInventory(p_145891_0_);

        if (iinventory != null) {
            Direction enumfacing = Direction.DOWN;

            if (isInventoryEmpty(iinventory, enumfacing)) {
                return false;
            }

            if (iinventory instanceof ISidedInventory isidedinventory) {
                int[] aint = isidedinventory.getSlotsForFace(enumfacing);

                for (int j : aint) {
                    if (pullItemFromSlot(p_145891_0_, iinventory, j, enumfacing)) {
                        return true;
                    }
                }
            } else {
                int j = iinventory.getSizeInventory();

                for (int k = 0; k < j; ++k) {
                    if (pullItemFromSlot(p_145891_0_, iinventory, k, enumfacing)) {
                        return true;
                    }
                }
            }
        } else {
            for (EntityItem entityitem : func_181556_a(p_145891_0_.getWorld(), p_145891_0_.getXPos(), p_145891_0_.getYPos() + 1.0D, p_145891_0_.getZPos())) {
                if (putDropInInventoryAllSlots(p_145891_0_, entityitem)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, Direction direction) {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);

        if (itemstack != null && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = putStackInInventoryAllSlots(hopper, inventoryIn.decrStackSize(index, 1), null);

            if (itemstack2 == null || itemstack2.stackSize == 0) {
                inventoryIn.markDirty();
                return true;
            }

            inventoryIn.setInventorySlotContents(index, itemstack1);
        }

        return false;
    }

    public static boolean putDropInInventoryAllSlots(IInventory p_145898_0_, EntityItem itemIn) {
        boolean flag = false;

        if (itemIn == null) {
            return false;
        } else {
            ItemStack itemstack = itemIn.getEntityItem().copy();
            ItemStack itemstack1 = putStackInInventoryAllSlots(p_145898_0_, itemstack, null);

            if (itemstack1 != null && itemstack1.stackSize != 0) {
                itemIn.setEntityItemStack(itemstack1);
            } else {
                flag = true;
                itemIn.setDead();
            }

            return flag;
        }
    }

    public static ItemStack putStackInInventoryAllSlots(IInventory inventoryIn, ItemStack stack, Direction side) {
        if (inventoryIn instanceof ISidedInventory isidedinventory && side != null) {
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int k = 0; k < aint.length && stack != null && stack.stackSize > 0; ++k) {
                stack = insertStack(inventoryIn, stack, aint[k], side);
            }
        } else {
            int i = inventoryIn.getSizeInventory();

            for (int j = 0; j < i && stack != null && stack.stackSize > 0; ++j) {
                stack = insertStack(inventoryIn, stack, j, side);
            }
        }

        if (stack != null && stack.stackSize == 0) {
            stack = null;
        }

        return stack;
    }

    private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side) {
        return inventoryIn.isItemValidForSlot(index, stack) && (!(inventoryIn instanceof ISidedInventory) || ((ISidedInventory) inventoryIn).canInsertItem(index, stack, side));
    }

    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side) {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory) inventoryIn).canExtractItem(index, stack, side);
    }

    private static ItemStack insertStack(IInventory inventoryIn, ItemStack stack, int index, Direction side) {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);

        if (canInsertItemInSlot(inventoryIn, stack, index, side)) {
            boolean flag = false;

            if (itemstack == null) {
                inventoryIn.setInventorySlotContents(index, stack);
                stack = null;
                flag = true;
            } else if (canCombine(itemstack, stack)) {
                int i = stack.getMaxStackSize() - itemstack.stackSize;
                int j = Math.min(stack.stackSize, i);
                stack.stackSize -= j;
                itemstack.stackSize += j;
                flag = j > 0;
            }

            if (flag) {
                if (inventoryIn instanceof TileEntityHopper tileentityhopper) {

                    if (tileentityhopper.mayTransfer()) {
                        tileentityhopper.transferCooldown = 8;
                    }

                    inventoryIn.markDirty();
                }

                inventoryIn.markDirty();
            }
        }

        return stack;
    }

    public static IInventory getHopperInventory(IHopper hopper) {
        return getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0D, hopper.getZPos());
    }

    public static List<EntityItem> func_181556_a(World p_181556_0_, double p_181556_1_, double p_181556_3_, double p_181556_5_) {
        return p_181556_0_.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(p_181556_1_ - 0.5D, p_181556_3_ - 0.5D, p_181556_5_ - 0.5D, p_181556_1_ + 0.5D, p_181556_3_ + 0.5D, p_181556_5_ + 0.5D), EntitySelectors.selectAnything);
    }

    public static IInventory getInventoryAtPosition(World worldIn, double x, double y, double z) {
        IInventory iinventory = null;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        BlockPos blockpos = new BlockPos(i, j, k);
        Block block = worldIn.getBlockState(blockpos).getBlock();

        if (block.hasTileEntity()) {
            TileEntity tileentity = worldIn.getTileEntity(blockpos);

            if (tileentity instanceof IInventory) {
                iinventory = (IInventory) tileentity;

                if (iinventory instanceof TileEntityChest && block instanceof BlockChest) {
                    iinventory = ((BlockChest) block).getLockableContainer(worldIn, blockpos);
                }
            }
        }

        if (iinventory == null) {
            List<Entity> list = worldIn.getEntitiesInAABBexcluding(null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.selectInventories);

            if (!list.isEmpty()) {
                iinventory = (IInventory) list.get(worldIn.rand.nextInt(list.size()));
            }
        }

        return iinventory;
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && (stack1.getMetadata() == stack2.getMetadata() && (stack1.stackSize <= stack1.getMaxStackSize() && ItemStack.areItemStackTagsEqual(stack1, stack2)));
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];

        if (compound.hasKey("CustomName", 8)) {
            customName = compound.getString("CustomName");
        }

        transferCooldown = compound.getInteger("TransferCooldown");

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot");

            if (j >= 0 && j < inventory.length) {
                inventory[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < inventory.length; ++i) {
            if (inventory[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);
        compound.setInteger("TransferCooldown", transferCooldown);

        if (hasCustomName()) {
            compound.setString("CustomName", customName);
        }
    }

    public void markDirty() {
        super.markDirty();
    }

    public int getSizeInventory() {
        return inventory.length;
    }

    public ItemStack getStackInSlot(int index) {
        return inventory[index];
    }

    public ItemStack decrStackSize(int index, int count) {
        if (inventory[index] != null) {
            if (inventory[index].stackSize <= count) {
                ItemStack itemstack1 = inventory[index];
                inventory[index] = null;
                return itemstack1;
            } else {
                ItemStack itemstack = inventory[index].splitStack(count);

                if (inventory[index].stackSize == 0) {
                    inventory[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        if (inventory[index] != null) {
            ItemStack itemstack = inventory[index];
            inventory[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    public String getName() {
        return hasCustomName() ? customName : "container.hopper";
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public void setCustomName(String customNameIn) {
        customName = customNameIn;
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

    public void update() {
        if (worldObj != null && !worldObj.isRemote) {
            --transferCooldown;

            if (!isOnTransferCooldown()) {
                transferCooldown = 0;
                updateHopper();
            }
        }
    }

    public boolean updateHopper() {
        if (worldObj != null && !worldObj.isRemote) {
            if (!isOnTransferCooldown() && BlockHopper.isEnabled(getBlockMetadata())) {
                boolean flag = false;

                if (!isEmpty()) {
                    flag = transferItemsOut();
                }

                if (!isFull()) {
                    flag = captureDroppedItems(this) || flag;
                }

                if (flag) {
                    transferCooldown = 8;
                    markDirty();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean isEmpty() {
        for (ItemStack itemstack : inventory) {
            if (itemstack != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isFull() {
        for (ItemStack itemstack : inventory) {
            if (itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private boolean transferItemsOut() {
        IInventory iinventory = getInventoryForHopperTransfer();

        if (iinventory == null) {
            return false;
        } else {
            Direction enumfacing = BlockHopper.getFacing(getBlockMetadata()).getOpposite();

            if (isInventoryFull(iinventory, enumfacing)) {
                return false;
            } else {
                for (int i = 0; i < getSizeInventory(); ++i) {
                    if (getStackInSlot(i) != null) {
                        ItemStack itemstack = getStackInSlot(i).copy();
                        ItemStack itemstack1 = putStackInInventoryAllSlots(iinventory, decrStackSize(i, 1), enumfacing);

                        if (itemstack1 == null || itemstack1.stackSize == 0) {
                            iinventory.markDirty();
                            return true;
                        }

                        setInventorySlotContents(i, itemstack);
                    }
                }

                return false;
            }
        }
    }

    private boolean isInventoryFull(IInventory inventoryIn, Direction side) {
        if (inventoryIn instanceof ISidedInventory isidedinventory) {
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int i : aint) {
                ItemStack itemstack1 = isidedinventory.getStackInSlot(i);

                if (itemstack1 == null || itemstack1.stackSize != itemstack1.getMaxStackSize()) {
                    return false;
                }
            }
        } else {
            int i = inventoryIn.getSizeInventory();

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = inventoryIn.getStackInSlot(j);

                if (itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
                    return false;
                }
            }
        }

        return true;
    }

    private IInventory getInventoryForHopperTransfer() {
        Direction enumfacing = BlockHopper.getFacing(getBlockMetadata());
        return getInventoryAtPosition(getWorld(), pos.getX() + enumfacing.getFrontOffsetX(), pos.getY() + enumfacing.getFrontOffsetY(), pos.getZ() + enumfacing.getFrontOffsetZ());
    }

    public double getXPos() {
        return (double) pos.getX() + 0.5D;
    }

    public double getYPos() {
        return (double) pos.getY() + 0.5D;
    }

    public double getZPos() {
        return (double) pos.getZ() + 0.5D;
    }

    public void setTransferCooldown(int ticks) {
        transferCooldown = ticks;
    }

    public boolean isOnTransferCooldown() {
        return transferCooldown > 0;
    }

    public boolean mayTransfer() {
        return transferCooldown <= 1;
    }

    public String getGuiID() {
        return "minecraft:hopper";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerHopper(playerInventory, this, playerIn);
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
        Arrays.fill(inventory, null);
    }
}
