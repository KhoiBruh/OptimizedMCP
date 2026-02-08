package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

import java.util.List;
import java.util.Random;

public class BlockDoublePlant extends BlockBush implements IGrowable {
    public static final PropertyEnum<PlantType> VARIANT = PropertyEnum.create("variant", PlantType.class);
    public static final PropertyEnum<BlockHalf> HALF = PropertyEnum.create("half", BlockHalf.class);
    public static final PropertyEnum<Direction> FACING = BlockDirectional.FACING;

    public BlockDoublePlant() {
        super(Material.vine);
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, PlantType.SUNFLOWER).withProperty(HALF, BlockHalf.LOWER).withProperty(FACING, Direction.NORTH));
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setUnlocalizedName("doublePlant");
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public PlantType getVariant(IBlockAccess worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this) {
            iblockstate = getActualState(iblockstate, worldIn, pos);
            return iblockstate.getValue(VARIANT);
        } else {
            return PlantType.FERN;
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
    }

    public boolean isReplaceable(World worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() != this) {
            return true;
        } else {
            PlantType blockdoubleplant$enumplanttype = getActualState(iblockstate, worldIn, pos).getValue(VARIANT);
            return blockdoubleplant$enumplanttype == PlantType.FERN || blockdoubleplant$enumplanttype == PlantType.GRASS;
        }
    }

    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!canBlockStay(worldIn, pos, state)) {
            boolean flag = state.getValue(HALF) == BlockHalf.UPPER;
            BlockPos blockpos = flag ? pos : pos.up();
            BlockPos blockpos1 = flag ? pos.down() : pos;
            Block block = flag ? this : worldIn.getBlockState(blockpos).getBlock();
            Block block1 = flag ? worldIn.getBlockState(blockpos1).getBlock() : this;

            if (block == this) {
                worldIn.setBlockState(blockpos, Blocks.air.getDefaultState(), 2);
            }

            if (block1 == this) {
                worldIn.setBlockState(blockpos1, Blocks.air.getDefaultState(), 3);

                if (!flag) {
                    dropBlockAsItem(worldIn, blockpos1, state, 0);
                }
            }
        }
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF) == BlockHalf.UPPER) {
            return worldIn.getBlockState(pos.down()).getBlock() == this;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos.up());
            return iblockstate.getBlock() == this && super.canBlockStay(worldIn, pos, iblockstate);
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (state.getValue(HALF) == BlockHalf.UPPER) {
            return null;
        } else {
            PlantType blockdoubleplant$enumplanttype = state.getValue(VARIANT);
            return blockdoubleplant$enumplanttype == PlantType.FERN ? null : (blockdoubleplant$enumplanttype == PlantType.GRASS ? (rand.nextInt(8) == 0 ? Items.wheat_seeds : null) : Item.getItemFromBlock(this));
        }
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(HALF) != BlockHalf.UPPER && state.getValue(VARIANT) != PlantType.GRASS ? state.getValue(VARIANT).getMeta() : 0;
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
        PlantType blockdoubleplant$enumplanttype = getVariant(worldIn, pos);
        return blockdoubleplant$enumplanttype != PlantType.GRASS && blockdoubleplant$enumplanttype != PlantType.FERN ? 16777215 : BiomeColorHelper.getGrassColorAtPos(worldIn, pos);
    }

    public void placeAt(World worldIn, BlockPos lowerPos, PlantType variant, int flags) {
        worldIn.setBlockState(lowerPos, getDefaultState().withProperty(HALF, BlockHalf.LOWER).withProperty(VARIANT, variant), flags);
        worldIn.setBlockState(lowerPos.up(), getDefaultState().withProperty(HALF, BlockHalf.UPPER), flags);
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos.up(), getDefaultState().withProperty(HALF, BlockHalf.UPPER), 2);
    }

    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        if (worldIn.isRemote || player.getCurrentEquippedItem() == null || player.getCurrentEquippedItem().getItem() != Items.shears || state.getValue(HALF) != BlockHalf.LOWER || !onHarvest(worldIn, pos, state, player)) {
            super.harvestBlock(worldIn, player, pos, state, te);
        }
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getValue(HALF) == BlockHalf.UPPER) {
            if (worldIn.getBlockState(pos.down()).getBlock() == this) {
                if (!player.capabilities.isCreativeMode) {
                    IBlockState iblockstate = worldIn.getBlockState(pos.down());
                    PlantType blockdoubleplant$enumplanttype = iblockstate.getValue(VARIANT);

                    if (blockdoubleplant$enumplanttype != PlantType.FERN && blockdoubleplant$enumplanttype != PlantType.GRASS) {
                        worldIn.destroyBlock(pos.down(), true);
                    } else if (!worldIn.isRemote) {
                        if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.shears) {
                            onHarvest(worldIn, pos, iblockstate, player);
                            worldIn.setBlockToAir(pos.down());
                        } else {
                            worldIn.destroyBlock(pos.down(), true);
                        }
                    } else {
                        worldIn.setBlockToAir(pos.down());
                    }
                } else {
                    worldIn.setBlockToAir(pos.down());
                }
            }
        } else if (player.capabilities.isCreativeMode && worldIn.getBlockState(pos.up()).getBlock() == this) {
            worldIn.setBlockState(pos.up(), Blocks.air.getDefaultState(), 2);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    private boolean onHarvest(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        PlantType blockdoubleplant$enumplanttype = state.getValue(VARIANT);

        if (blockdoubleplant$enumplanttype != PlantType.FERN && blockdoubleplant$enumplanttype != PlantType.GRASS) {
            return false;
        } else {
            player.triggerAchievement(StatList.mineBlockStatArray[Block.getIdFromBlock(this)]);
            int i = (blockdoubleplant$enumplanttype == PlantType.GRASS ? BlockTallGrass.Type.GRASS : BlockTallGrass.Type.FERN).getMeta();
            spawnAsEntity(worldIn, pos, new ItemStack(Blocks.tallgrass, 2, i));
            return true;
        }
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (PlantType blockdoubleplant$enumplanttype : PlantType.values()) {
            list.add(new ItemStack(itemIn, 1, blockdoubleplant$enumplanttype.getMeta()));
        }
    }

    public int getDamageValue(World worldIn, BlockPos pos) {
        return getVariant(worldIn, pos).getMeta();
    }

    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        PlantType blockdoubleplant$enumplanttype = getVariant(worldIn, pos);
        return blockdoubleplant$enumplanttype != PlantType.GRASS && blockdoubleplant$enumplanttype != PlantType.FERN;
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        spawnAsEntity(worldIn, pos, new ItemStack(this, 1, getVariant(worldIn, pos).getMeta()));
    }

    public IBlockState getStateFromMeta(int meta) {
        return (meta & 8) > 0 ? getDefaultState().withProperty(HALF, BlockHalf.UPPER) : getDefaultState().withProperty(HALF, BlockHalf.LOWER).withProperty(VARIANT, PlantType.byMetadata(meta & 7));
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(HALF) == BlockHalf.UPPER) {
            IBlockState iblockstate = worldIn.getBlockState(pos.down());

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(VARIANT, iblockstate.getValue(VARIANT));
            }
        }

        return state;
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF) == BlockHalf.UPPER ? 8 | state.getValue(FACING).getHorizontalIndex() : state.getValue(VARIANT).getMeta();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, HALF, VARIANT, FACING);
    }

    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    public enum BlockHalf implements IStringSerializable {
        UPPER,
        LOWER;

        public String toString() {
            return getName();
        }

        public String getName() {
            return this == UPPER ? "upper" : "lower";
        }
    }

    public enum PlantType implements IStringSerializable {
        SUNFLOWER(0, "sunflower"),
        SYRINGA(1, "syringa"),
        GRASS(2, "double_grass", "grass"),
        FERN(3, "double_fern", "fern"),
        ROSE(4, "double_rose", "rose"),
        PAEONIA(5, "paeonia");

        private static final PlantType[] META_LOOKUP = new PlantType[values().length];

        static {
            for (PlantType blockdoubleplant$enumplanttype : values()) {
                META_LOOKUP[blockdoubleplant$enumplanttype.meta] = blockdoubleplant$enumplanttype;
            }
        }

        private final int meta;
        private final String name;
        private final String unlocalizedName;

        PlantType(int meta, String name) {
            this(meta, name, name);
        }

        PlantType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public static PlantType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
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
