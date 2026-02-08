package net.minecraft.tileentity;

import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;

import java.util.Arrays;
import java.util.List;

public class TileEntityBrewingStand extends TileEntityLockable implements ITickable, ISidedInventory {
    private static final int[] inputSlots = new int[]{3};
    private static final int[] outputSlots = new int[]{0, 1, 2};
    public String customName;
    private ItemStack[] brewingItemStacks = new ItemStack[4];
    private int brewTime;
    private boolean[] filledSlots;
    private Item ingredientID;

    public String getName() {
        return hasCustomName() ? customName : "container.brewing";
    }

    public void setName(String name) {
        customName = name;
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public int getSizeInventory() {
        return brewingItemStacks.length;
    }

    public void update() {
        if (brewTime > 0) {
            --brewTime;

            if (brewTime == 0) {
                brewPotions();
                markDirty();
            } else if (!canBrew()) {
                brewTime = 0;
                markDirty();
            } else if (ingredientID != brewingItemStacks[3].getItem()) {
                brewTime = 0;
                markDirty();
            }
        } else if (canBrew()) {
            brewTime = 400;
            ingredientID = brewingItemStacks[3].getItem();
        }

        if (!worldObj.isRemote) {
            boolean[] aboolean = func_174902_m();

            if (!Arrays.equals(aboolean, filledSlots)) {
                filledSlots = aboolean;
                IBlockState iblockstate = worldObj.getBlockState(getPos());

                if (!(iblockstate.getBlock() instanceof BlockBrewingStand)) {
                    return;
                }

                for (int i = 0; i < BlockBrewingStand.HAS_BOTTLE.length; ++i) {
                    iblockstate = iblockstate.withProperty(BlockBrewingStand.HAS_BOTTLE[i],
                            aboolean[i]);
                }

                worldObj.setBlockState(pos, iblockstate, 2);
            }
        }
    }

    private boolean canBrew() {
        if (brewingItemStacks[3] != null && brewingItemStacks[3].stackSize > 0) {
            ItemStack itemstack = brewingItemStacks[3];

            if (!itemstack.getItem().isPotionIngredient(itemstack)) {
                return false;
            } else {
                boolean flag = false;

                for (int i = 0; i < 3; ++i) {
                    if (brewingItemStacks[i] != null && brewingItemStacks[i].getItem() == Items.potionitem) {
                        int j = brewingItemStacks[i].getMetadata();
                        int k = getPotionResult(j, itemstack);

                        if (!ItemPotion.isSplash(j) && ItemPotion.isSplash(k)) {
                            flag = true;
                            break;
                        }

                        List<PotionEffect> list = Items.potionitem.getEffects(j);
                        List<PotionEffect> list1 = Items.potionitem.getEffects(k);

                        if ((j <= 0 || list != list1) && (list == null || !list.equals(list1) && list1 != null)
                                && j != k) {
                            flag = true;
                            break;
                        }
                    }
                }

                return flag;
            }
        } else {
            return false;
        }
    }

    private void brewPotions() {
        if (canBrew()) {
            ItemStack itemstack = brewingItemStacks[3];

            for (int i = 0; i < 3; ++i) {
                if (brewingItemStacks[i] != null && brewingItemStacks[i].getItem() == Items.potionitem) {
                    int j = brewingItemStacks[i].getMetadata();
                    int k = getPotionResult(j, itemstack);
                    List<PotionEffect> list = Items.potionitem.getEffects(j);
                    List<PotionEffect> list1 = Items.potionitem.getEffects(k);

                    if (j > 0 && list == list1 || list != null && (list.equals(list1) || list1 == null)) {
                        if (!ItemPotion.isSplash(j) && ItemPotion.isSplash(k)) {
                            brewingItemStacks[i].setItemDamage(k);
                        }
                    } else if (j != k) {
                        brewingItemStacks[i].setItemDamage(k);
                    }
                }
            }

            if (itemstack.getItem().hasContainerItem()) {
                brewingItemStacks[3] = new ItemStack(itemstack.getItem().getContainerItem());
            } else {
                --brewingItemStacks[3].stackSize;

                if (brewingItemStacks[3].stackSize <= 0) {
                    brewingItemStacks[3] = null;
                }
            }
        }
    }

    private int getPotionResult(int meta, ItemStack stack) {
        return stack == null ? meta
                : (stack.getItem().isPotionIngredient(stack)
                ? PotionHelper.applyIngredient(meta, stack.getItem().getPotionEffect(stack))
                : meta);
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        brewingItemStacks = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot");

            if (j >= 0 && j < brewingItemStacks.length) {
                brewingItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        brewTime = compound.getShort("BrewTime");

        if (compound.hasKey("CustomName", 8)) {
            customName = compound.getString("CustomName");
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setShort("BrewTime", (short) brewTime);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < brewingItemStacks.length; ++i) {
            if (brewingItemStacks[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                brewingItemStacks[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);

        if (hasCustomName()) {
            compound.setString("CustomName", customName);
        }
    }

    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < brewingItemStacks.length ? brewingItemStacks[index] : null;
    }

    public ItemStack decrStackSize(int index, int count) {
        if (index >= 0 && index < brewingItemStacks.length) {
            ItemStack itemstack = brewingItemStacks[index];
            brewingItemStacks[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        if (index >= 0 && index < brewingItemStacks.length) {
            ItemStack itemstack = brewingItemStacks[index];
            brewingItemStacks[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < brewingItemStacks.length) {
            brewingItemStacks[index] = stack;
        }
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
                (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 3 ? stack.getItem().isPotionIngredient(stack)
                : stack.getItem() == Items.potionitem || stack.getItem() == Items.glass_bottle;
    }

    public boolean[] func_174902_m() {
        boolean[] aboolean = new boolean[3];

        for (int i = 0; i < 3; ++i) {
            if (brewingItemStacks[i] != null) {
                aboolean[i] = true;
            }
        }

        return aboolean;
    }

    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? inputSlots : outputSlots;
    }

    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public String getGuiID() {
        return "minecraft:brewing_stand";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerBrewingStand(playerInventory, this);
    }

    public int getField(int id) {
        return switch (id) {
            case 0 -> brewTime;
            default -> 0;
        };
    }

    public void setField(int id, int value) {
        if (id == 0) {
            brewTime = value;
        }
    }

    public int getFieldCount() {
        return 1;
    }

    public void clear() {
        Arrays.fill(brewingItemStacks, null);
    }
}
