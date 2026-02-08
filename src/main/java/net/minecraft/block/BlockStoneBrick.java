package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.List;

public class BlockStoneBrick extends Block {
    public static final PropertyEnum<Type> VARIANT = PropertyEnum.create("variant", Type.class);
    public static final int DEFAULT_META = Type.DEFAULT.getMetadata();
    public static final int MOSSY_META = Type.MOSSY.getMetadata();
    public static final int CRACKED_META = Type.CRACKED.getMetadata();
    public static final int CHISELED_META = Type.CHISELED.getMetadata();

    public BlockStoneBrick() {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, Type.DEFAULT));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (Type blockstonebrick$enumtype : Type.values()) {
            list.add(new ItemStack(itemIn, 1, blockstonebrick$enumtype.getMetadata()));
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, Type.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT);
    }

    public enum Type implements IStringSerializable {
        DEFAULT(0, "stonebrick", "default"),
        MOSSY(1, "mossy_stonebrick", "mossy"),
        CRACKED(2, "cracked_stonebrick", "cracked"),
        CHISELED(3, "chiseled_stonebrick", "chiseled");

        private static final Type[] META_LOOKUP = new Type[values().length];

        static {
            for (Type blockstonebrick$enumtype : values()) {
                META_LOOKUP[blockstonebrick$enumtype.meta] = blockstonebrick$enumtype;
            }
        }

        private final int meta;
        private final String name;
        private final String unlocalizedName;

        Type(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public static Type byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public int getMetadata() {
            return meta;
        }

        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public String getUnlocalizedName() {
            return unlocalizedName;
        }
    }
}
