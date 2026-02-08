package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;

import java.util.List;

public class BlockQuartz extends Block {
    public static final PropertyEnum<Type> VARIANT = PropertyEnum.create("variant", Type.class);

    public BlockQuartz() {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, Type.DEFAULT));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (meta == Type.LINES_Y.getMetadata()) {
            return switch (facing.getAxis()) {
                case Z -> getDefaultState().withProperty(VARIANT, Type.LINES_Z);
                case X -> getDefaultState().withProperty(VARIANT, Type.LINES_X);
                default -> getDefaultState().withProperty(VARIANT, Type.LINES_Y);
            };
        } else {
            return meta == Type.CHISELED.getMetadata() ? getDefaultState().withProperty(VARIANT, Type.CHISELED) : getDefaultState().withProperty(VARIANT, Type.DEFAULT);
        }
    }

    public int damageDropped(IBlockState state) {
        Type blockquartz$enumtype = state.getValue(VARIANT);
        return blockquartz$enumtype != Type.LINES_X && blockquartz$enumtype != Type.LINES_Z ? blockquartz$enumtype.getMetadata() : Type.LINES_Y.getMetadata();
    }

    protected ItemStack createStackedBlock(IBlockState state) {
        Type blockquartz$enumtype = state.getValue(VARIANT);
        return blockquartz$enumtype != Type.LINES_X && blockquartz$enumtype != Type.LINES_Z ? super.createStackedBlock(state) : new ItemStack(Item.getItemFromBlock(this), 1, Type.LINES_Y.getMetadata());
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(itemIn, 1, Type.DEFAULT.getMetadata()));
        list.add(new ItemStack(itemIn, 1, Type.CHISELED.getMetadata()));
        list.add(new ItemStack(itemIn, 1, Type.LINES_Y.getMetadata()));
    }

    public MapColor getMapColor(IBlockState state) {
        return MapColor.quartzColor;
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
        DEFAULT(0, "default", "default"),
        CHISELED(1, "chiseled", "chiseled"),
        LINES_Y(2, "lines_y", "lines"),
        LINES_X(3, "lines_x", "lines"),
        LINES_Z(4, "lines_z", "lines");

        private static final Type[] META_LOOKUP = new Type[values().length];

        static {
            for (Type blockquartz$enumtype : values()) {
                META_LOOKUP[blockquartz$enumtype.meta] = blockquartz$enumtype;
            }
        }

        private final int meta;
        private final String field_176805_h;
        private final String unlocalizedName;

        Type(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            field_176805_h = name;
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
            return unlocalizedName;
        }

        public String getName() {
            return field_176805_h;
        }
    }
}
