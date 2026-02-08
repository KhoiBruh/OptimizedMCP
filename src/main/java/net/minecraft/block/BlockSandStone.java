package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.List;

public class BlockSandStone extends Block {
    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public BlockSandStone() {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(TYPE, Type.DEFAULT));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (Type blocksandstone$enumtype : Type.values()) {
            list.add(new ItemStack(itemIn, 1, blocksandstone$enumtype.getMetadata()));
        }
    }

    public MapColor getMapColor(IBlockState state) {
        return MapColor.sandColor;
    }

    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(TYPE, Type.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, TYPE);
    }

    public enum Type implements IStringSerializable {
        DEFAULT(0, "sandstone", "default"),
        CHISELED(1, "chiseled_sandstone", "chiseled"),
        SMOOTH(2, "smooth_sandstone", "smooth");

        private static final Type[] META_LOOKUP = new Type[values().length];

        static {
            for (Type blocksandstone$enumtype : values()) {
                META_LOOKUP[blocksandstone$enumtype.metadata] = blocksandstone$enumtype;
            }
        }

        private final int metadata;
        private final String name;
        private final String unlocalizedName;

        Type(int meta, String name, String unlocalizedName) {
            metadata = meta;
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
            return metadata;
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
