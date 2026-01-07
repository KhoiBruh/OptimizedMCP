package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;

public class SlotCrafting extends Slot {
    private final InventoryCrafting craftMatrix;
    private final EntityPlayer thePlayer;
    private int amountCrafted;

    public SlotCrafting(EntityPlayer player, InventoryCrafting craftingInventory, IInventory p_i45790_3_, int slotIndex, int xPosition, int yPosition) {
        super(p_i45790_3_, slotIndex, xPosition, yPosition);
        thePlayer = player;
        craftMatrix = craftingInventory;
    }

    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    public ItemStack decrStackSize(int amount) {
        if (getHasStack()) {
            amountCrafted += Math.min(amount, getStack().stackSize);
        }

        return super.decrStackSize(amount);
    }

    protected void onCrafting(ItemStack stack, int amount) {
        amountCrafted += amount;
        onCrafting(stack);
    }

    protected void onCrafting(ItemStack stack) {
        if (amountCrafted > 0) {
            stack.onCrafting(thePlayer.worldObj, thePlayer, amountCrafted);
        }

        amountCrafted = 0;

        if (stack.getItem() == Item.getItemFromBlock(Blocks.crafting_table)) {
            thePlayer.triggerAchievement(AchievementList.buildWorkBench);
        }

        if (stack.getItem() instanceof ItemPickaxe) {
            thePlayer.triggerAchievement(AchievementList.buildPickaxe);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.furnace)) {
            thePlayer.triggerAchievement(AchievementList.buildFurnace);
        }

        if (stack.getItem() instanceof ItemHoe) {
            thePlayer.triggerAchievement(AchievementList.buildHoe);
        }

        if (stack.getItem() == Items.bread) {
            thePlayer.triggerAchievement(AchievementList.makeBread);
        }

        if (stack.getItem() == Items.cake) {
            thePlayer.triggerAchievement(AchievementList.bakeCake);
        }

        if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe) stack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD) {
            thePlayer.triggerAchievement(AchievementList.buildBetterPickaxe);
        }

        if (stack.getItem() instanceof ItemSword) {
            thePlayer.triggerAchievement(AchievementList.buildSword);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table)) {
            thePlayer.triggerAchievement(AchievementList.enchantments);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf)) {
            thePlayer.triggerAchievement(AchievementList.bookcase);
        }

        if (stack.getItem() == Items.golden_apple && stack.getMetadata() == 1) {
            thePlayer.triggerAchievement(AchievementList.overpowered);
        }
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
        onCrafting(stack);
        ItemStack[] aitemstack = CraftingManager.getInstance().func_180303_b(craftMatrix, playerIn.worldObj);

        for (int i = 0; i < aitemstack.length; ++i) {
            ItemStack itemstack = craftMatrix.getStackInSlot(i);
            ItemStack itemstack1 = aitemstack[i];

            if (itemstack != null) {
                craftMatrix.decrStackSize(i, 1);
            }

            if (itemstack1 != null) {
                if (craftMatrix.getStackInSlot(i) == null) {
                    craftMatrix.setInventorySlotContents(i, itemstack1);
                } else if (!thePlayer.inventory.addItemStackToInventory(itemstack1)) {
                    thePlayer.dropPlayerItemWithRandomChoice(itemstack1, false);
                }
            }
        }
    }
}
