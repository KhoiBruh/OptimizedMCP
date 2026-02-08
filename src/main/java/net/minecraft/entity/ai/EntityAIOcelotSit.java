package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityAIOcelotSit extends EntityAIMoveToBlock {
    private final EntityOcelot ocelot;

    public EntityAIOcelotSit(EntityOcelot ocelotIn, double p_i45315_2_) {
        super(ocelotIn, p_i45315_2_, 8);
        ocelot = ocelotIn;
    }

    public boolean shouldExecute() {
        return ocelot.isTamed() && !ocelot.isSitting() && super.shouldExecute();
    }

    public boolean continueExecuting() {
        return super.continueExecuting();
    }

    public void startExecuting() {
        super.startExecuting();
        ocelot.getAISit().setSitting(false);
    }

    public void resetTask() {
        super.resetTask();
        ocelot.setSitting(false);
    }

    public void updateTask() {
        super.updateTask();
        ocelot.getAISit().setSitting(false);

        if (!getIsAboveDestination()) {
            ocelot.setSitting(false);
        } else if (!ocelot.isSitting()) {
            ocelot.setSitting(true);
        }
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        if (!worldIn.isAirBlock(pos.up())) {
            return false;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if (block == Blocks.chest) {
                TileEntity tileentity = worldIn.getTileEntity(pos);

                return tileentity instanceof TileEntityChest && ((TileEntityChest) tileentity).numPlayersUsing < 1;
            } else {
                if (block == Blocks.lit_furnace) {
                    return true;
                }

                return block == Blocks.bed && iblockstate.getValue(BlockBed.PART) != BlockBed.PartType.HEAD;
            }
        }
    }
}
