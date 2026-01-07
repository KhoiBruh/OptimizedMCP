package net.minecraft.item;

import net.minecraft.block.BlockLeaves;

public class ItemLeaves extends ItemBlock {
    private final BlockLeaves leaves;

    public ItemLeaves(BlockLeaves block) {
        super(block);
        leaves = block;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public int getMetadata(int damage) {
        return damage | 4;
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return leaves.getRenderColor(leaves.getStateFromMeta(stack.getMetadata()));
    }

    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + leaves.getWoodType(stack.getMetadata()).getUnlocalizedName();
    }
}
