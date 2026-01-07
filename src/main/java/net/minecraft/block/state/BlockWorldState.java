package net.minecraft.block.state;

import com.google.common.base.Predicate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockWorldState {
    private final World world;
    private final BlockPos pos;
    private final boolean field_181628_c;
    private IBlockState state;
    private TileEntity tileEntity;
    private boolean tileEntityInitialized;

    public BlockWorldState(World worldIn, BlockPos posIn, boolean p_i46451_3_) {
        world = worldIn;
        pos = posIn;
        field_181628_c = p_i46451_3_;
    }

    public static Predicate<BlockWorldState> hasState(final Predicate<IBlockState> predicatesIn) {
        return p_apply_1_ -> p_apply_1_ != null && predicatesIn.apply(p_apply_1_.getBlockState());
    }

    public IBlockState getBlockState() {
        if (state == null && (field_181628_c || world.isBlockLoaded(pos))) {
            state = world.getBlockState(pos);
        }

        return state;
    }

    public TileEntity getTileEntity() {
        if (tileEntity == null && !tileEntityInitialized) {
            tileEntity = world.getTileEntity(pos);
            tileEntityInitialized = true;
        }

        return tileEntity;
    }

    public BlockPos getPos() {
        return pos;
    }
}
