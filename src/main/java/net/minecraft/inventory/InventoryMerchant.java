package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.Arrays;

public class InventoryMerchant implements IInventory {
    private final IMerchant theMerchant;
    private final EntityPlayer thePlayer;
    private final ItemStack[] theInventory = new ItemStack[3];
    private MerchantRecipe currentRecipe;
    private int currentRecipeIndex;

    public InventoryMerchant(EntityPlayer thePlayerIn, IMerchant theMerchantIn) {
        thePlayer = thePlayerIn;
        theMerchant = theMerchantIn;
    }

    public int getSizeInventory() {
        return theInventory.length;
    }

    public ItemStack getStackInSlot(int index) {
        return theInventory[index];
    }

    public ItemStack decrStackSize(int index, int count) {
        if (theInventory[index] != null) {
            if (index == 2) {
                ItemStack itemstack2 = theInventory[index];
                theInventory[index] = null;
                return itemstack2;
            } else if (theInventory[index].stackSize <= count) {
                ItemStack itemstack1 = theInventory[index];
                theInventory[index] = null;

                if (inventoryResetNeededOnSlotChange(index)) {
                    resetRecipeAndSlots();
                }

                return itemstack1;
            } else {
                ItemStack itemstack = theInventory[index].splitStack(count);

                if (theInventory[index].stackSize == 0) {
                    theInventory[index] = null;
                }

                if (inventoryResetNeededOnSlotChange(index)) {
                    resetRecipeAndSlots();
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    private boolean inventoryResetNeededOnSlotChange(int p_70469_1_) {
        return p_70469_1_ == 0 || p_70469_1_ == 1;
    }

    public ItemStack removeStackFromSlot(int index) {
        if (theInventory[index] != null) {
            ItemStack itemstack = theInventory[index];
            theInventory[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        theInventory[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        if (inventoryResetNeededOnSlotChange(index)) {
            resetRecipeAndSlots();
        }
    }

    public String getName() {
        return "mob.villager";
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

    public boolean isUseableByPlayer(EntityPlayer player) {
        return theMerchant.getCustomer() == player;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public void markDirty() {
        resetRecipeAndSlots();
    }

    public void resetRecipeAndSlots() {
        currentRecipe = null;
        ItemStack itemstack = theInventory[0];
        ItemStack itemstack1 = theInventory[1];

        if (itemstack == null) {
            itemstack = itemstack1;
            itemstack1 = null;
        }

        if (itemstack == null) {
            setInventorySlotContents(2, null);
        } else {
            MerchantRecipeList merchantrecipelist = theMerchant.getRecipes(thePlayer);

            if (merchantrecipelist != null) {
                MerchantRecipe merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack, itemstack1, currentRecipeIndex);

                if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled()) {
                    currentRecipe = merchantrecipe;
                    setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
                } else if (itemstack1 != null) {
                    merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack1, itemstack, currentRecipeIndex);

                    if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled()) {
                        currentRecipe = merchantrecipe;
                        setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
                    } else {
                        setInventorySlotContents(2, null);
                    }
                } else {
                    setInventorySlotContents(2, null);
                }
            }
        }

        theMerchant.verifySellingItem(getStackInSlot(2));
    }

    public MerchantRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipeIndex(int currentRecipeIndexIn) {
        currentRecipeIndex = currentRecipeIndexIn;
        resetRecipeAndSlots();
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
        Arrays.fill(theInventory, null);
    }
}
