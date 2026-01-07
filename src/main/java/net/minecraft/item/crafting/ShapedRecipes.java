package net.minecraft.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ShapedRecipes implements IRecipe {
    private final int recipeWidth;
    private final int recipeHeight;
    private final ItemStack[] recipeItems;
    private final ItemStack recipeOutput;
    private boolean copyIngredientNBT;

    public ShapedRecipes(int width, int height, ItemStack[] p_i1917_3_, ItemStack output) {
        recipeWidth = width;
        recipeHeight = height;
        recipeItems = p_i1917_3_;
        recipeOutput = output;
    }

    public ItemStack getRecipeOutput() {
        return recipeOutput;
    }

    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < aitemstack.length; ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);

            if (itemstack != null && itemstack.getItem().hasContainerItem()) {
                aitemstack[i] = new ItemStack(itemstack.getItem().getContainerItem());
            }
        }

        return aitemstack;
    }

    public boolean matches(InventoryCrafting inv, World worldIn) {
        for (int i = 0; i <= 3 - recipeWidth; ++i) {
            for (int j = 0; j <= 3 - recipeHeight; ++j) {
                if (checkMatch(inv, i, j, true)) {
                    return true;
                }

                if (checkMatch(inv, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(InventoryCrafting p_77573_1_, int p_77573_2_, int p_77573_3_, boolean p_77573_4_) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int k = i - p_77573_2_;
                int l = j - p_77573_3_;
                ItemStack itemstack = null;

                if (k >= 0 && l >= 0 && k < recipeWidth && l < recipeHeight) {
                    if (p_77573_4_) {
                        itemstack = recipeItems[recipeWidth - k - 1 + l * recipeWidth];
                    } else {
                        itemstack = recipeItems[k + l * recipeWidth];
                    }
                }

                ItemStack itemstack1 = p_77573_1_.getStackInRowAndColumn(i, j);

                if (itemstack1 != null || itemstack != null) {
                    if (itemstack1 == null && itemstack != null || itemstack1 != null && itemstack == null) {
                        return false;
                    }

                    if (itemstack.getItem() != itemstack1.getItem()) {
                        return false;
                    }

                    if (itemstack.getMetadata() != 32767 && itemstack.getMetadata() != itemstack1.getMetadata()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack itemstack = recipeOutput.copy();

        if (copyIngredientNBT) {
            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                ItemStack itemstack1 = inv.getStackInSlot(i);

                if (itemstack1 != null && itemstack1.hasTagCompound()) {
                    itemstack.setTagCompound((NBTTagCompound) itemstack1.getTagCompound().copy());
                }
            }
        }

        return itemstack;
    }

    public int getRecipeSize() {
        return recipeWidth * recipeHeight;
    }
}
