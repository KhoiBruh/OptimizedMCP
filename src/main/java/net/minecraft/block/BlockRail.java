package net.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockRail extends BlockRailBase {
    public static final PropertyEnum<RailDirection> SHAPE = PropertyEnum.create("shape", RailDirection.class);

    protected BlockRail() {
        super(false);
        setDefaultState(blockState.getBaseState().withProperty(SHAPE, RailDirection.NORTH_SOUTH));
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (neighborBlock.canProvidePower() && (new BlockRailBase.Rail(worldIn, pos, state)).countAdjacentRails() == 3) {
            func_176564_a(worldIn, pos, state, false);
        }
    }

    public IProperty<RailDirection> getShapeProperty() {
        return SHAPE;
    }

    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(SHAPE, RailDirection.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(SHAPE).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, SHAPE);
    }
}
