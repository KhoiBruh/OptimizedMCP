package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public interface IMerchant {
    EntityPlayer getCustomer();

    void setCustomer(EntityPlayer p_70932_1_);

    MerchantRecipeList getRecipes(EntityPlayer p_70934_1_);

    void setRecipes(MerchantRecipeList recipeList);

    void useRecipe(MerchantRecipe recipe);

    void verifySellingItem(ItemStack stack);

    IChatComponent getDisplayName();
}
