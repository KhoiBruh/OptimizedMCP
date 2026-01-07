package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.village.MerchantRecipe;

public class SlotMerchantResult extends Slot {
    private final InventoryMerchant theMerchantInventory;
    private final IMerchant theMerchant;
    private final EntityPlayer thePlayer;
    private int field_75231_g;

    public SlotMerchantResult(EntityPlayer player, IMerchant merchant, InventoryMerchant merchantInventory, int slotIndex, int xPosition, int yPosition) {
        super(merchantInventory, slotIndex, xPosition, yPosition);
        thePlayer = player;
        theMerchant = merchant;
        theMerchantInventory = merchantInventory;
    }

    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    public ItemStack decrStackSize(int amount) {
        if (getHasStack()) {
            field_75231_g += Math.min(amount, getStack().stackSize);
        }

        return super.decrStackSize(amount);
    }

    protected void onCrafting(ItemStack stack, int amount) {
        field_75231_g += amount;
        onCrafting(stack);
    }

    protected void onCrafting(ItemStack stack) {
        stack.onCrafting(thePlayer.worldObj, thePlayer, field_75231_g);
        field_75231_g = 0;
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
        onCrafting(stack);
        MerchantRecipe merchantrecipe = theMerchantInventory.getCurrentRecipe();

        if (merchantrecipe != null) {
            ItemStack itemstack = theMerchantInventory.getStackInSlot(0);
            ItemStack itemstack1 = theMerchantInventory.getStackInSlot(1);

            if (doTrade(merchantrecipe, itemstack, itemstack1) || doTrade(merchantrecipe, itemstack1, itemstack)) {
                theMerchant.useRecipe(merchantrecipe);
                playerIn.triggerAchievement(StatList.timesTradedWithVillagerStat);

                if (itemstack != null && itemstack.stackSize <= 0) {
                    itemstack = null;
                }

                if (itemstack1 != null && itemstack1.stackSize <= 0) {
                    itemstack1 = null;
                }

                theMerchantInventory.setInventorySlotContents(0, itemstack);
                theMerchantInventory.setInventorySlotContents(1, itemstack1);
            }
        }
    }

    private boolean doTrade(MerchantRecipe trade, ItemStack firstItem, ItemStack secondItem) {
        ItemStack itemstack = trade.getItemToBuy();
        ItemStack itemstack1 = trade.getSecondItemToBuy();

        if (firstItem != null && firstItem.getItem() == itemstack.getItem()) {
            if (itemstack1 != null && secondItem != null && itemstack1.getItem() == secondItem.getItem()) {
                firstItem.stackSize -= itemstack.stackSize;
                secondItem.stackSize -= itemstack1.stackSize;
                return true;
            }

            if (itemstack1 == null && secondItem == null) {
                firstItem.stackSize -= itemstack.stackSize;
                return true;
            }
        }

        return false;
    }
}
