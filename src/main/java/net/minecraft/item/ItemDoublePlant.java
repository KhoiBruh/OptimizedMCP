package net.minecraft.item;

import com.google.common.base.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.world.ColorizerGrass;

public class ItemDoublePlant extends ItemMultiTexture {
    public ItemDoublePlant(Block block, Block block2, Function<ItemStack, String> nameFunction) {
        super(block, block2, nameFunction);
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        BlockDoublePlant.PlantType blockdoubleplant$enumplanttype = BlockDoublePlant.PlantType.byMetadata(stack.getMetadata());
        return blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.GRASS && blockdoubleplant$enumplanttype != BlockDoublePlant.PlantType.FERN ? super.getColorFromItemStack(stack, renderPass) : ColorizerGrass.getGrassColor(0.5D, 1.0D);
    }
}
