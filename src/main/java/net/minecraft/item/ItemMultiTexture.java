package net.minecraft.item;

import com.google.common.base.Function;
import net.minecraft.block.Block;

public class ItemMultiTexture extends ItemBlock {
    protected final Block theBlock;
    protected final Function<ItemStack, String> nameFunction;

    public ItemMultiTexture(Block block, Block block2, Function<ItemStack, String> nameFunction) {
        super(block);
        theBlock = block2;
        this.nameFunction = nameFunction;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public ItemMultiTexture(Block block, Block block2, final String[] namesByMeta) {
        this(block, block2, new Function<ItemStack, String>() {
            public String apply(ItemStack p_apply_1_) {
                int i = p_apply_1_.getMetadata();

                if (i < 0 || i >= namesByMeta.length) {
                    i = 0;
                }

                return namesByMeta[i];
            }
        });
    }

    public int getMetadata(int damage) {
        return damage;
    }

    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + nameFunction.apply(stack);
    }
}
