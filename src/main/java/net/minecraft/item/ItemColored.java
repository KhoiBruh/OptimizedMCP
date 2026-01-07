package net.minecraft.item;

import net.minecraft.block.Block;

public class ItemColored extends ItemBlock {
    private final Block coloredBlock;
    private String[] subtypeNames;

    public ItemColored(Block block, boolean hasSubtypes) {
        super(block);
        coloredBlock = block;

        if (hasSubtypes) {
            setMaxDamage(0);
            setHasSubtypes(true);
        }
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return coloredBlock.getRenderColor(coloredBlock.getStateFromMeta(stack.getMetadata()));
    }

    public int getMetadata(int damage) {
        return damage;
    }

    public ItemColored setSubtypeNames(String[] names) {
        subtypeNames = names;
        return this;
    }

    public String getUnlocalizedName(ItemStack stack) {
        if (subtypeNames == null) {
            return super.getUnlocalizedName(stack);
        } else {
            int i = stack.getMetadata();
            return i >= 0 && i < subtypeNames.length ? super.getUnlocalizedName(stack) + "." + subtypeNames[i] : super.getUnlocalizedName(stack);
        }
    }
}
