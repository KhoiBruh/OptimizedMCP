package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;

import java.util.Arrays;

public class TileEntityFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
    private static final int[] slotsTop = new int[]{0};
    private static final int[] slotsBottom = new int[]{2, 1};
    private static final int[] slotsSides = new int[]{1};
    public String furnaceCustomName;
    private ItemStack[] furnaceItemStacks = new ItemStack[3];
    private int furnaceBurnTime;
    private int currentItemBurnTime;
    private int cookTime;
    private int totalCookTime;

    public static boolean isBurning(IInventory p_174903_0_) {
        return p_174903_0_.getField(0) > 0;
    }

    public static int getItemBurnTime(ItemStack p_145952_0_) {
        if (p_145952_0_ == null) {
            return 0;
        } else {
            Item item = p_145952_0_.getItem();

            if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air) {
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.wooden_slab) {
                    return 150;
                }

                if (block.getMaterial() == Material.wood) {
                    return 300;
                }

                if (block == Blocks.coal_block) {
                    return 16000;
                }
            }

            return item instanceof ItemTool && ((ItemTool) item).getToolMaterialName().equals("WOOD") ? 200
                    : (item instanceof ItemSword && ((ItemSword) item).getToolMaterialName().equals("WOOD") ? 200
                    : (item instanceof ItemHoe && ((ItemHoe) item).getMaterialName().equals("WOOD") ? 200
                    : (item == Items.stick ? 100
                    : (item == Items.coal ? 1600
                    : (item == Items.lava_bucket ? 20000
                    : (item == Item.getItemFromBlock(Blocks.sapling) ? 100
                    : (item == Items.blaze_rod ? 2400 : 0)))))));
        }
    }

    public static boolean isItemFuel(ItemStack p_145954_0_) {
        return getItemBurnTime(p_145954_0_) > 0;
    }

    public int getSizeInventory() {
        return furnaceItemStacks.length;
    }

    public ItemStack getStackInSlot(int index) {
        return furnaceItemStacks[index];
    }

    public ItemStack decrStackSize(int index, int count) {
        if (furnaceItemStacks[index] != null) {
            if (furnaceItemStacks[index].stackSize <= count) {
                ItemStack itemstack1 = furnaceItemStacks[index];
                furnaceItemStacks[index] = null;
                return itemstack1;
            } else {
                ItemStack itemstack = furnaceItemStacks[index].splitStack(count);

                if (furnaceItemStacks[index].stackSize == 0) {
                    furnaceItemStacks[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        if (furnaceItemStacks[index] != null) {
            ItemStack itemstack = furnaceItemStacks[index];
            furnaceItemStacks[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        boolean flag = stack != null && stack.isItemEqual(furnaceItemStacks[index])
                && ItemStack.areItemStackTagsEqual(stack, furnaceItemStacks[index]);
        furnaceItemStacks[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        if (index == 0 && !flag) {
            totalCookTime = getCookTime(stack);
            cookTime = 0;
            markDirty();
        }
    }

    public String getName() {
        return hasCustomName() ? furnaceCustomName : "container.furnace";
    }

    public boolean hasCustomName() {
        return furnaceCustomName != null && !furnaceCustomName.isEmpty();
    }

    public void setCustomInventoryName(String p_145951_1_) {
        furnaceCustomName = p_145951_1_;
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        furnaceItemStacks = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot");

            if (j >= 0 && j < furnaceItemStacks.length) {
                furnaceItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        furnaceBurnTime = compound.getShort("BurnTime");
        cookTime = compound.getShort("CookTime");
        totalCookTime = compound.getShort("CookTimeTotal");
        currentItemBurnTime = getItemBurnTime(furnaceItemStacks[1]);

        if (compound.hasKey("CustomName", 8)) {
            furnaceCustomName = compound.getString("CustomName");
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setShort("BurnTime", (short) furnaceBurnTime);
        compound.setShort("CookTime", (short) cookTime);
        compound.setShort("CookTimeTotal", (short) totalCookTime);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < furnaceItemStacks.length; ++i) {
            if (furnaceItemStacks[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                furnaceItemStacks[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);

        if (hasCustomName()) {
            compound.setString("CustomName", furnaceCustomName);
        }
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean isBurning() {
        return furnaceBurnTime > 0;
    }

    public void update() {
        boolean flag = isBurning();
        boolean flag1 = false;

        if (isBurning()) {
            --furnaceBurnTime;
        }

        if (!worldObj.isRemote) {
            if (isBurning() || furnaceItemStacks[1] != null && furnaceItemStacks[0] != null) {
                if (!isBurning() && canSmelt()) {
                    currentItemBurnTime = furnaceBurnTime = getItemBurnTime(furnaceItemStacks[1]);

                    if (isBurning()) {
                        flag1 = true;

                        if (furnaceItemStacks[1] != null) {
                            --furnaceItemStacks[1].stackSize;

                            if (furnaceItemStacks[1].stackSize == 0) {
                                Item item = furnaceItemStacks[1].getItem().getContainerItem();
                                furnaceItemStacks[1] = item != null ? new ItemStack(item) : null;
                            }
                        }
                    }
                }

                if (isBurning() && canSmelt()) {
                    ++cookTime;

                    if (cookTime == totalCookTime) {
                        cookTime = 0;
                        totalCookTime = getCookTime(furnaceItemStacks[0]);
                        smeltItem();
                        flag1 = true;
                    }
                } else {
                    cookTime = 0;
                }
            } else if (!isBurning() && cookTime > 0) {
                cookTime = MathHelper.clamp_int(cookTime - 2, 0, totalCookTime);
            }

            if (flag != isBurning()) {
                flag1 = true;
                BlockFurnace.setState(isBurning(), worldObj, pos);
            }
        }

        if (flag1) {
            markDirty();
        }
    }

    public int getCookTime(ItemStack stack) {
        return 200;
    }

    private boolean canSmelt() {
        if (furnaceItemStacks[0] == null) {
            return false;
        } else {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(furnaceItemStacks[0]);
            return itemstack != null && (furnaceItemStacks[2] == null || (furnaceItemStacks[2].isItemEqual(itemstack) && (furnaceItemStacks[2].stackSize < getInventoryStackLimit()
                    && furnaceItemStacks[2].stackSize < furnaceItemStacks[2]
                    .getMaxStackSize() || furnaceItemStacks[2].stackSize < itemstack
                    .getMaxStackSize())));
        }
    }

    public void smeltItem() {
        if (canSmelt()) {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(furnaceItemStacks[0]);

            if (furnaceItemStacks[2] == null) {
                furnaceItemStacks[2] = itemstack.copy();
            } else if (furnaceItemStacks[2].getItem() == itemstack.getItem()) {
                ++furnaceItemStacks[2].stackSize;
            }

            if (furnaceItemStacks[0].getItem() == Item.getItemFromBlock(Blocks.sponge)
                    && furnaceItemStacks[0].getMetadata() == 1 && furnaceItemStacks[1] != null
                    && furnaceItemStacks[1].getItem() == Items.bucket) {
                furnaceItemStacks[1] = new ItemStack(Items.water_bucket);
            }

            --furnaceItemStacks[0].stackSize;

            if (furnaceItemStacks[0].stackSize <= 0) {
                furnaceItemStacks[0] = null;
            }
        }
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
        return index != 2 && (index != 1 || isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack));
    }

    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? slotsBottom : (side == Direction.UP ? slotsTop : slotsSides);
    }

    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        if (direction == Direction.DOWN && index == 1) {
            Item item = stack.getItem();

            return item == Items.water_bucket || item == Items.bucket;
        }

        return true;
    }

    public String getGuiID() {
        return "minecraft:furnace";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerFurnace(playerInventory, this);
    }

    public int getField(int id) {
        return switch (id) {
            case 0 -> furnaceBurnTime;
            case 1 -> currentItemBurnTime;
            case 2 -> cookTime;
            case 3 -> totalCookTime;
            default -> 0;
        };
    }

    public void setField(int id, int value) {
        switch (id) {
            case 0:
                furnaceBurnTime = value;
                break;

            case 1:
                currentItemBurnTime = value;
                break;

            case 2:
                cookTime = value;
                break;

            case 3:
                totalCookTime = value;
        }
    }

    public int getFieldCount() {
        return 4;
    }

    public void clear() {
        Arrays.fill(furnaceItemStacks, null);
    }
}
