package net.minecraft.entity.player;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;

import java.util.Arrays;

public class InventoryPlayer implements IInventory {
    public ItemStack[] mainInventory = new ItemStack[36];
    public ItemStack[] armorInventory = new ItemStack[4];
    public int currentItem;
    public EntityPlayer player;
    public boolean inventoryChanged;
    private ItemStack itemStack;

    public InventoryPlayer(EntityPlayer playerIn) {
        player = playerIn;
    }

    public static int getHotbarSize() {
        return 9;
    }

    public ItemStack getCurrentItem() {
        return currentItem < 9 && currentItem >= 0 ? mainInventory[currentItem] : null;
    }

    private int getInventorySlotContainItem(Item itemIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemIn) {
                return i;
            }
        }

        return -1;
    }

    private int getInventorySlotContainItemAndDamage(Item itemIn, int metadataIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemIn && mainInventory[i].getMetadata() == metadataIn) {
                return i;
            }
        }

        return -1;
    }

    private int storeItemStack(ItemStack itemStackIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemStackIn.getItem() && mainInventory[i].isStackable() && mainInventory[i].stackSize < mainInventory[i].getMaxStackSize() && mainInventory[i].stackSize < getInventoryStackLimit() && (!mainInventory[i].getHasSubtypes() || mainInventory[i].getMetadata() == itemStackIn.getMetadata()) && ItemStack.areItemStackTagsEqual(mainInventory[i], itemStackIn)) {
                return i;
            }
        }

        return -1;
    }

    public int getFirstEmptyStack() {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public void setCurrentItem(Item itemIn, int metadataIn, boolean isMetaSpecific, boolean p_146030_4_) {
        ItemStack itemstack = getCurrentItem();
        int i = isMetaSpecific ? getInventorySlotContainItemAndDamage(itemIn, metadataIn) : getInventorySlotContainItem(itemIn);

        if (i >= 0 && i < 9) {
            currentItem = i;
        } else if (p_146030_4_ && itemIn != null) {
            int j = getFirstEmptyStack();

            if (j >= 0 && j < 9) {
                currentItem = j;
            }

            if (itemstack == null || !itemstack.isItemEnchantable() || getInventorySlotContainItemAndDamage(itemstack.getItem(), itemstack.getItemDamage()) != currentItem) {
                int k = getInventorySlotContainItemAndDamage(itemIn, metadataIn);
                int l;

                if (k >= 0) {
                    l = mainInventory[k].stackSize;
                    mainInventory[k] = mainInventory[currentItem];
                } else {
                    l = 1;
                }

                mainInventory[currentItem] = new ItemStack(itemIn, l, metadataIn);
            }
        }
    }

    public void changeCurrentItem(int direction) {
        if (direction > 0) {
            direction = 1;
        }

        if (direction < 0) {
            direction = -1;
        }

        for (currentItem -= direction; currentItem < 0; currentItem += 9) {
        }

        while (currentItem >= 9) {
            currentItem -= 9;
        }
    }

    public int clearMatchingItems(Item itemIn, int metadataIn, int removeCount, NBTTagCompound itemNBT) {
        int i = 0;

        for (int j = 0; j < mainInventory.length; ++j) {
            ItemStack itemstack = mainInventory[j];

            if (itemstack != null && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.func_181123_a(itemNBT, itemstack.getTagCompound(), true))) {
                int k = removeCount <= 0 ? itemstack.stackSize : Math.min(removeCount - i, itemstack.stackSize);
                i += k;

                if (removeCount != 0) {
                    mainInventory[j].stackSize -= k;

                    if (mainInventory[j].stackSize == 0) {
                        mainInventory[j] = null;
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }

        for (int l = 0; l < armorInventory.length; ++l) {
            ItemStack itemstack1 = armorInventory[l];

            if (itemstack1 != null && (itemIn == null || itemstack1.getItem() == itemIn) && (metadataIn <= -1 || itemstack1.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.func_181123_a(itemNBT, itemstack1.getTagCompound(), false))) {
                int j1 = removeCount <= 0 ? itemstack1.stackSize : Math.min(removeCount - i, itemstack1.stackSize);
                i += j1;

                if (removeCount != 0) {
                    armorInventory[l].stackSize -= j1;

                    if (armorInventory[l].stackSize == 0) {
                        armorInventory[l] = null;
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }

        if (itemStack != null) {
            if (itemIn != null && itemStack.getItem() != itemIn) {
                return i;
            }

            if (metadataIn > -1 && itemStack.getMetadata() != metadataIn) {
                return i;
            }

            if (itemNBT != null && !NBTUtil.func_181123_a(itemNBT, itemStack.getTagCompound(), false)) {
                return i;
            }

            int i1 = removeCount <= 0 ? itemStack.stackSize : Math.min(removeCount - i, itemStack.stackSize);
            i += i1;

            if (removeCount != 0) {
                itemStack.stackSize -= i1;

                if (itemStack.stackSize == 0) {
                    itemStack = null;
                }

                if (removeCount > 0 && i >= removeCount) {
                    return i;
                }
            }
        }

        return i;
    }

    private int storePartialItemStack(ItemStack itemStackIn) {
        Item item = itemStackIn.getItem();
        int i = itemStackIn.stackSize;
        int j = storeItemStack(itemStackIn);

        if (j < 0) {
            j = getFirstEmptyStack();
        }

        if (j < 0) {
            return i;
        } else {
            if (mainInventory[j] == null) {
                mainInventory[j] = new ItemStack(item, 0, itemStackIn.getMetadata());

                if (itemStackIn.hasTagCompound()) {
                    mainInventory[j].setTagCompound((NBTTagCompound) itemStackIn.getTagCompound().copy());
                }
            }

            int k = Math.min(i, mainInventory[j].getMaxStackSize() - mainInventory[j].stackSize);

            if (k > getInventoryStackLimit() - mainInventory[j].stackSize) {
                k = getInventoryStackLimit() - mainInventory[j].stackSize;
            }

            if (k == 0) {
                return i;
            } else {
                i = i - k;
                mainInventory[j].stackSize += k;
                mainInventory[j].animationsToGo = 5;
                return i;
            }
        }
    }

    public void decrementAnimations() {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null) {
                mainInventory[i].updateAnimation(player.worldObj, player, i, currentItem == i);
            }
        }
    }

    public boolean consumeInventoryItem(Item itemIn) {
        int i = getInventorySlotContainItem(itemIn);

        if (i < 0) {
            return false;
        } else {
            if (--mainInventory[i].stackSize <= 0) {
                mainInventory[i] = null;
            }

            return true;
        }
    }

    public boolean hasItem(Item itemIn) {
        int i = getInventorySlotContainItem(itemIn);
        return i >= 0;
    }

    public boolean addItemStackToInventory(final ItemStack itemStackIn) {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
            try {
                if (itemStackIn.isItemDamaged()) {
                    int j = getFirstEmptyStack();

                    if (j >= 0) {
                        mainInventory[j] = ItemStack.copyItemStack(itemStackIn);
                        mainInventory[j].animationsToGo = 5;
                        itemStackIn.stackSize = 0;
                        return true;
                    } else if (player.capabilities.isCreativeMode) {
                        itemStackIn.stackSize = 0;
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;

                    while (true) {
                        i = itemStackIn.stackSize;
                        itemStackIn.stackSize = storePartialItemStack(itemStackIn);

                        if (itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= i) {
                            break;
                        }
                    }

                    if (itemStackIn.stackSize == i && player.capabilities.isCreativeMode) {
                        itemStackIn.stackSize = 0;
                        return true;
                    } else {
                        return itemStackIn.stackSize < i;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(itemStackIn.getItem()));
                crashreportcategory.addCrashSection("Item data", itemStackIn.getMetadata());
                crashreportcategory.addCrashSectionCallable("Item name", itemStackIn::getDisplayName);
                throw new ReportedException(crashreport);
            }
        } else {
            return false;
        }
    }

    public ItemStack decrStackSize(int index, int count) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= mainInventory.length) {
            aitemstack = armorInventory;
            index -= mainInventory.length;
        }

        if (aitemstack[index] != null) {
            if (aitemstack[index].stackSize <= count) {
                ItemStack itemstack1 = aitemstack[index];
                aitemstack[index] = null;
                return itemstack1;
            } else {
                ItemStack itemstack = aitemstack[index].splitStack(count);

                if (aitemstack[index].stackSize == 0) {
                    aitemstack[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= mainInventory.length) {
            aitemstack = armorInventory;
            index -= mainInventory.length;
        }

        if (aitemstack[index] != null) {
            ItemStack itemstack = aitemstack[index];
            aitemstack[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = armorInventory;
        }

        aitemstack[index] = stack;
    }

    public float getStrVsBlock(Block blockIn) {
        float f = 1.0F;

        if (mainInventory[currentItem] != null) {
            f *= mainInventory[currentItem].getStrVsBlock(blockIn);
        }

        return f;
    }

    public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                mainInventory[i].writeToNBT(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            if (armorInventory[j] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) (j + 100));
                armorInventory[j].writeToNBT(nbttagcompound1);
                nbtTagListIn.appendTag(nbttagcompound1);
            }
        }

        return nbtTagListIn;
    }

    public void readFromNBT(NBTTagList nbtTagListIn) {
        mainInventory = new ItemStack[36];
        armorInventory = new ItemStack[4];

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null) {
                if (j >= 0 && j < mainInventory.length) {
                    mainInventory[j] = itemstack;
                }

                if (j >= 100 && j < armorInventory.length + 100) {
                    armorInventory[j - 100] = itemstack;
                }
            }
        }
    }

    public int getSizeInventory() {
        return mainInventory.length + 4;
    }

    public ItemStack getStackInSlot(int index) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = armorInventory;
        }

        return aitemstack[index];
    }

    public String getName() {
        return "container.inventory";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean canHeldItemHarvest(Block blockIn) {
        if (blockIn.getMaterial().isToolNotRequired()) {
            return true;
        } else {
            ItemStack itemstack = getStackInSlot(currentItem);
            return itemstack != null && itemstack.canHarvestBlock(blockIn);
        }
    }

    public ItemStack armorItemInSlot(int slotIn) {
        return armorInventory[slotIn];
    }

    public int getTotalArmorValue() {
        int i = 0;

        for (ItemStack stack : armorInventory) {
            if (stack != null && stack.getItem() instanceof ItemArmor) {
                int k = ((ItemArmor) stack.getItem()).damageReduceAmount;
                i += k;
            }
        }

        return i;
    }

    public void damageArmor(float damage) {
        damage = damage / 4.0F;

        if (damage < 1.0F) {
            damage = 1.0F;
        }

        for (int i = 0; i < armorInventory.length; ++i) {
            if (armorInventory[i] != null && armorInventory[i].getItem() instanceof ItemArmor) {
                armorInventory[i].damageItem((int) damage, player);

                if (armorInventory[i].stackSize == 0) {
                    armorInventory[i] = null;
                }
            }
        }
    }

    public void dropAllItems() {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null) {
                player.dropItem(mainInventory[i], true, false);
                mainInventory[i] = null;
            }
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            if (armorInventory[j] != null) {
                player.dropItem(armorInventory[j], true, false);
                armorInventory[j] = null;
            }
        }
    }

    public void markDirty() {
        inventoryChanged = true;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStackIn) {
        itemStack = itemStackIn;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return !this.player.isDead && player.getDistanceSqToEntity(this.player) <= 64.0D;
    }

    public boolean hasItemStack(ItemStack itemStackIn) {
        for (ItemStack value : armorInventory) {
            if (value != null && value.isItemEqual(itemStackIn)) {
                return true;
            }
        }

        for (ItemStack stack : mainInventory) {
            if (stack != null && stack.isItemEqual(itemStackIn)) {
                return true;
            }
        }

        return false;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public void copyInventory(InventoryPlayer playerInventory) {
        for (int i = 0; i < mainInventory.length; ++i) {
            mainInventory[i] = ItemStack.copyItemStack(playerInventory.mainInventory[i]);
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            armorInventory[j] = ItemStack.copyItemStack(playerInventory.armorInventory[j]);
        }

        currentItem = playerInventory.currentItem;
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
        Arrays.fill(mainInventory, null);

        Arrays.fill(armorInventory, null);
    }
}
