package net.minecraft.item.crafting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FurnaceRecipes {
    private static final FurnaceRecipes smeltingBase = new FurnaceRecipes();
    private final Map<ItemStack, ItemStack> smeltingList = new HashMap<>();
    private final Map<ItemStack, Float> experienceList = new HashMap<>();

    private FurnaceRecipes() {
        addSmeltingRecipeForBlock(Blocks.iron_ore, new ItemStack(Items.iron_ingot), 0.7F);
        addSmeltingRecipeForBlock(Blocks.gold_ore, new ItemStack(Items.gold_ingot), 1.0F);
        addSmeltingRecipeForBlock(Blocks.diamond_ore, new ItemStack(Items.diamond), 1.0F);
        addSmeltingRecipeForBlock(Blocks.sand, new ItemStack(Blocks.glass), 0.1F);
        addSmelting(Items.porkchop, new ItemStack(Items.cooked_porkchop), 0.35F);
        addSmelting(Items.beef, new ItemStack(Items.cooked_beef), 0.35F);
        addSmelting(Items.chicken, new ItemStack(Items.cooked_chicken), 0.35F);
        addSmelting(Items.rabbit, new ItemStack(Items.cooked_rabbit), 0.35F);
        addSmelting(Items.mutton, new ItemStack(Items.cooked_mutton), 0.35F);
        addSmeltingRecipeForBlock(Blocks.cobblestone, new ItemStack(Blocks.stone), 0.1F);
        addSmeltingRecipe(new ItemStack(Blocks.stonebrick, 1, BlockStoneBrick.DEFAULT_META), new ItemStack(Blocks.stonebrick, 1, BlockStoneBrick.CRACKED_META), 0.1F);
        addSmelting(Items.clay_ball, new ItemStack(Items.brick), 0.3F);
        addSmeltingRecipeForBlock(Blocks.clay, new ItemStack(Blocks.hardened_clay), 0.35F);
        addSmeltingRecipeForBlock(Blocks.cactus, new ItemStack(Items.dye, 1, DyeColor.GREEN.getDyeDamage()), 0.2F);
        addSmeltingRecipeForBlock(Blocks.log, new ItemStack(Items.coal, 1, 1), 0.15F);
        addSmeltingRecipeForBlock(Blocks.log2, new ItemStack(Items.coal, 1, 1), 0.15F);
        addSmeltingRecipeForBlock(Blocks.emerald_ore, new ItemStack(Items.emerald), 1.0F);
        addSmelting(Items.potato, new ItemStack(Items.baked_potato), 0.35F);
        addSmeltingRecipeForBlock(Blocks.netherrack, new ItemStack(Items.netherbrick), 0.1F);
        addSmeltingRecipe(new ItemStack(Blocks.sponge, 1, 1), new ItemStack(Blocks.sponge, 1, 0), 0.15F);

        for (ItemFishFood.FishType itemfishfood$fishtype : ItemFishFood.FishType.values()) {
            if (itemfishfood$fishtype.canCook()) {
                addSmeltingRecipe(new ItemStack(Items.fish, 1, itemfishfood$fishtype.getMetadata()), new ItemStack(Items.cooked_fish, 1, itemfishfood$fishtype.getMetadata()), 0.35F);
            }
        }

        addSmeltingRecipeForBlock(Blocks.coal_ore, new ItemStack(Items.coal), 0.1F);
        addSmeltingRecipeForBlock(Blocks.redstone_ore, new ItemStack(Items.redstone), 0.7F);
        addSmeltingRecipeForBlock(Blocks.lapis_ore, new ItemStack(Items.dye, 1, DyeColor.BLUE.getDyeDamage()), 0.2F);
        addSmeltingRecipeForBlock(Blocks.quartz_ore, new ItemStack(Items.quartz), 0.2F);
    }

    public static FurnaceRecipes instance() {
        return smeltingBase;
    }

    public void addSmeltingRecipeForBlock(Block input, ItemStack stack, float experience) {
        addSmelting(Item.getItemFromBlock(input), stack, experience);
    }

    public void addSmelting(Item input, ItemStack stack, float experience) {
        addSmeltingRecipe(new ItemStack(input, 1, 32767), stack, experience);
    }

    public void addSmeltingRecipe(ItemStack input, ItemStack stack, float experience) {
        smeltingList.put(input, stack);
        experienceList.put(stack, experience);
    }

    public ItemStack getSmeltingResult(ItemStack stack) {
        for (Entry<ItemStack, ItemStack> entry : smeltingList.entrySet()) {
            if (compareItemStacks(stack, entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    private boolean compareItemStacks(ItemStack stack1, ItemStack stack2) {
        return stack2.getItem() == stack1.getItem() && (stack2.getMetadata() == 32767 || stack2.getMetadata() == stack1.getMetadata());
    }

    public Map<ItemStack, ItemStack> getSmeltingList() {
        return smeltingList;
    }

    public float getSmeltingExperience(ItemStack stack) {
        for (Entry<ItemStack, Float> entry : experienceList.entrySet()) {
            if (compareItemStacks(stack, entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0.0F;
    }
}
