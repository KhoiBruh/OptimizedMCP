package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public abstract class BlockStoneSlabNew extends BlockSlab {
    public static final PropertyBool SEAMLESS = PropertyBool.create("seamless");
    public static final PropertyEnum<Type> VARIANT = PropertyEnum.create("variant", Type.class);

    public BlockStoneSlabNew() {
        super(Material.rock);
        IBlockState iblockstate = blockState.getBaseState();

        if (isDouble()) {
            iblockstate = iblockstate.withProperty(SEAMLESS, Boolean.FALSE);
        } else {
            iblockstate = iblockstate.withProperty(HALF, BlockHalf.BOTTOM);
        }

        setDefaultState(iblockstate.withProperty(VARIANT, BlockStoneSlabNew.Type.RED_SANDSTONE));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public String getLocalizedName() {
        return StatCollector.translateToLocal(getUnlocalizedName() + ".red_sandstone.name");
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.stone_slab2);
    }

    public Item getItem(World worldIn, BlockPos pos) {
        return Item.getItemFromBlock(Blocks.stone_slab2);
    }

    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName() + "." + BlockStoneSlabNew.Type.byMetadata(meta).getUnlocalizedName();
    }

    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    public Object getVariant(ItemStack stack) {
        return BlockStoneSlabNew.Type.byMetadata(stack.getMetadata() & 7);
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        if (itemIn != Item.getItemFromBlock(Blocks.double_stone_slab2)) {
            for (Type blockstoneslabnew$enumtype : BlockStoneSlabNew.Type.values()) {
                list.add(new ItemStack(itemIn, 1, blockstoneslabnew$enumtype.getMetadata()));
            }
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = getDefaultState().withProperty(VARIANT, BlockStoneSlabNew.Type.byMetadata(meta & 7));

        if (isDouble()) {
            iblockstate = iblockstate.withProperty(SEAMLESS, (meta & 8) != 0);
        } else {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockHalf.BOTTOM : BlockHalf.TOP);
        }

        return iblockstate;
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(VARIANT).getMetadata();

        if (isDouble()) {
            if (state.getValue(SEAMLESS)) {
                i |= 8;
            }
        } else if (state.getValue(HALF) == BlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState() {
        return isDouble() ? new BlockState(this, SEAMLESS, VARIANT) : new BlockState(this, HALF, VARIANT);
    }

    public MapColor getMapColor(IBlockState state) {
        return state.getValue(VARIANT).func_181068_c();
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    public enum Type implements IStringSerializable {
        RED_SANDSTONE(0, "red_sandstone", BlockSand.Type.RED_SAND.getMapColor());

        private static final Type[] META_LOOKUP = new Type[values().length];

        static {
            for (Type blockstoneslabnew$enumtype : values()) {
                META_LOOKUP[blockstoneslabnew$enumtype.meta] = blockstoneslabnew$enumtype;
            }
        }

        private final int meta;
        private final String name;
        private final MapColor field_181069_e;

        Type(int p_i46391_3_, String p_i46391_4_, MapColor p_i46391_5_) {
            meta = p_i46391_3_;
            name = p_i46391_4_;
            field_181069_e = p_i46391_5_;
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

        public MapColor func_181068_c() {
            return field_181069_e;
        }

        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public String getUnlocalizedName() {
            return name;
        }
    }
}
