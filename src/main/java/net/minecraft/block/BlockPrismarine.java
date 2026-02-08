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
import net.minecraft.util.StatCollector;

import java.util.List;

public class BlockPrismarine extends Block {
    public static final PropertyEnum<Type> VARIANT = PropertyEnum.create("variant", Type.class);
    public static final int ROUGH_META = Type.ROUGH.getMetadata();
    public static final int BRICKS_META = Type.BRICKS.getMetadata();
    public static final int DARK_META = Type.DARK.getMetadata();

    public BlockPrismarine() {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, Type.ROUGH));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public String getLocalizedName() {
        return StatCollector.translateToLocal(getUnlocalizedName() + "." + Type.ROUGH.getUnlocalizedName() + ".name");
    }

    public MapColor getMapColor(IBlockState state) {
        return state.getValue(VARIANT) == Type.ROUGH ? MapColor.cyanColor : MapColor.diamondColor;
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT);
    }

    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, Type.byMetadata(meta));
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(itemIn, 1, ROUGH_META));
        list.add(new ItemStack(itemIn, 1, BRICKS_META));
        list.add(new ItemStack(itemIn, 1, DARK_META));
    }

    public enum Type implements IStringSerializable {
        ROUGH(0, "prismarine", "rough"),
        BRICKS(1, "prismarine_bricks", "bricks"),
        DARK(2, "dark_prismarine", "dark");

        private static final Type[] META_LOOKUP = new Type[values().length];

        static {
            for (Type blockprismarine$enumtype : values()) {
                META_LOOKUP[blockprismarine$enumtype.meta] = blockprismarine$enumtype;
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
