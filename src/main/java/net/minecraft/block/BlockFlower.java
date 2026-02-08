package net.minecraft.block;

import com.google.common.collect.Lists;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.List;

public abstract class BlockFlower extends BlockBush {
    protected PropertyEnum<FlowerType> type;

    protected BlockFlower() {
        setDefaultState(blockState.getBaseState().withProperty(getTypeProperty(), getBlockType() == FlowerColor.RED ? FlowerType.POPPY : FlowerType.DANDELION));
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(getTypeProperty()).getMeta();
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (FlowerType blockflower$enumflowertype : FlowerType.getTypes(getBlockType())) {
            list.add(new ItemStack(itemIn, 1, blockflower$enumflowertype.getMeta()));
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getTypeProperty(), FlowerType.getType(getBlockType(), meta));
    }

    public abstract FlowerColor getBlockType();

    public IProperty<FlowerType> getTypeProperty() {
        if (type == null) {
            type = PropertyEnum.create("type", FlowerType.class, p_apply_1_ -> p_apply_1_.getBlockType() == getBlockType());
        }

        return type;
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(getTypeProperty()).getMeta();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, getTypeProperty());
    }

    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    public enum FlowerColor {
        YELLOW,
        RED;

        public BlockFlower getBlock() {
            return this == YELLOW ? Blocks.yellow_flower : Blocks.red_flower;
        }
    }

    public enum FlowerType implements IStringSerializable {
        DANDELION(FlowerColor.YELLOW, 0, "dandelion"),
        POPPY(FlowerColor.RED, 0, "poppy"),
        BLUE_ORCHID(FlowerColor.RED, 1, "blue_orchid", "blueOrchid"),
        ALLIUM(FlowerColor.RED, 2, "allium"),
        HOUSTONIA(FlowerColor.RED, 3, "houstonia"),
        RED_TULIP(FlowerColor.RED, 4, "red_tulip", "tulipRed"),
        ORANGE_TULIP(FlowerColor.RED, 5, "orange_tulip", "tulipOrange"),
        WHITE_TULIP(FlowerColor.RED, 6, "white_tulip", "tulipWhite"),
        PINK_TULIP(FlowerColor.RED, 7, "pink_tulip", "tulipPink"),
        OXEYE_DAISY(FlowerColor.RED, 8, "oxeye_daisy", "oxeyeDaisy");

        private static final FlowerType[][] TYPES_FOR_BLOCK = new FlowerType[FlowerColor.values().length][];

        static {
            for (final FlowerColor blockflower$enumflowercolor : FlowerColor.values()) {
                List<FlowerType> flowerTypes = Lists.newArrayList(values()).stream().filter(flowerType -> flowerType.blockType == blockflower$enumflowercolor).toList();
                TYPES_FOR_BLOCK[blockflower$enumflowercolor.ordinal()] = flowerTypes.toArray(new FlowerType[0]);
            }
        }

        private final FlowerColor blockType;
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        FlowerType(FlowerColor blockType, int meta, String name) {
            this(blockType, meta, name, name);
        }

        FlowerType(FlowerColor blockType, int meta, String name, String unlocalizedName) {
            this.blockType = blockType;
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public static FlowerType getType(FlowerColor blockType, int meta) {
            FlowerType[] ablockflower$enumflowertype = TYPES_FOR_BLOCK[blockType.ordinal()];

            if (meta < 0 || meta >= ablockflower$enumflowertype.length) {
                meta = 0;
            }

            return ablockflower$enumflowertype[meta];
        }

        public static FlowerType[] getTypes(FlowerColor flowerColor) {
            return TYPES_FOR_BLOCK[flowerColor.ordinal()];
        }

        public FlowerColor getBlockType() {
            return blockType;
        }

        public int getMeta() {
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
