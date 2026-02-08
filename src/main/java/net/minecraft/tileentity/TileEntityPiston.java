package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPiston extends TileEntity implements ITickable {
    private IBlockState pistonState;
    private Direction pistonFacing;
    private boolean extending;
    private boolean shouldHeadBeRendered;
    private float progress;
    private float lastProgress;
    private final List<Entity> field_174933_k = new ArrayList<>();

    public TileEntityPiston() {
    }

    public TileEntityPiston(IBlockState pistonStateIn, Direction pistonFacingIn, boolean extendingIn, boolean shouldHeadBeRenderedIn) {
        pistonState = pistonStateIn;
        pistonFacing = pistonFacingIn;
        extending = extendingIn;
        shouldHeadBeRendered = shouldHeadBeRenderedIn;
    }

    public IBlockState getPistonState() {
        return pistonState;
    }

    public int getBlockMetadata() {
        return 0;
    }

    public boolean isExtending() {
        return extending;
    }

    public Direction getFacing() {
        return pistonFacing;
    }

    public boolean shouldPistonHeadBeRendered() {
        return shouldHeadBeRendered;
    }

    public float getProgress(float ticks) {
        if (ticks > 1.0F) {
            ticks = 1.0F;
        }

        return lastProgress + (progress - lastProgress) * ticks;
    }

    public float getOffsetX(float ticks) {
        return extending ? (getProgress(ticks) - 1.0F) * (float) pistonFacing.getFrontOffsetX() : (1.0F - getProgress(ticks)) * (float) pistonFacing.getFrontOffsetX();
    }

    public float getOffsetY(float ticks) {
        return extending ? (getProgress(ticks) - 1.0F) * (float) pistonFacing.getFrontOffsetY() : (1.0F - getProgress(ticks)) * (float) pistonFacing.getFrontOffsetY();
    }

    public float getOffsetZ(float ticks) {
        return extending ? (getProgress(ticks) - 1.0F) * (float) pistonFacing.getFrontOffsetZ() : (1.0F - getProgress(ticks)) * (float) pistonFacing.getFrontOffsetZ();
    }

    private void launchWithSlimeBlock(float p_145863_1_, float p_145863_2_) {
        if (extending) {
            p_145863_1_ = 1.0F - p_145863_1_;
        } else {
            --p_145863_1_;
        }

        AxisAlignedBB axisalignedbb = Blocks.piston_extension.getBoundingBox(worldObj, pos, pistonState, p_145863_1_, pistonFacing);

        if (axisalignedbb != null) {
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

            if (!list.isEmpty()) {
                field_174933_k.addAll(list);

                for (Entity entity : field_174933_k) {
                    if (pistonState.getBlock() == Blocks.slime_block && extending) {
                        switch (pistonFacing.getAxis()) {
                            case X:
                                entity.motionX = pistonFacing.getFrontOffsetX();
                                break;

                            case Y:
                                entity.motionY = pistonFacing.getFrontOffsetY();
                                break;

                            case Z:
                                entity.motionZ = pistonFacing.getFrontOffsetZ();
                        }
                    } else {
                        entity.moveEntity(p_145863_2_ * (float) pistonFacing.getFrontOffsetX(), p_145863_2_ * (float) pistonFacing.getFrontOffsetY(), p_145863_2_ * (float) pistonFacing.getFrontOffsetZ());
                    }
                }

                field_174933_k.clear();
            }
        }
    }

    public void clearPistonTileEntity() {
        if (lastProgress < 1.0F && worldObj != null) {
            lastProgress = progress = 1.0F;
            worldObj.removeTileEntity(pos);
            invalidate();

            if (worldObj.getBlockState(pos).getBlock() == Blocks.piston_extension) {
                worldObj.setBlockState(pos, pistonState, 3);
                worldObj.notifyBlockOfStateChange(pos, pistonState.getBlock());
            }
        }
    }

    public void update() {
        lastProgress = progress;

        if (lastProgress >= 1.0F) {
            launchWithSlimeBlock(1.0F, 0.25F);
            worldObj.removeTileEntity(pos);
            invalidate();

            if (worldObj.getBlockState(pos).getBlock() == Blocks.piston_extension) {
                worldObj.setBlockState(pos, pistonState, 3);
                worldObj.notifyBlockOfStateChange(pos, pistonState.getBlock());
            }
        } else {
            progress += 0.5F;

            if (progress >= 1.0F) {
                progress = 1.0F;
            }

            if (extending) {
                launchWithSlimeBlock(progress, progress - lastProgress + 0.0625F);
            }
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        pistonState = Block.getBlockById(compound.getInteger("blockId")).getStateFromMeta(compound.getInteger("blockData"));
        pistonFacing = Direction.getFront(compound.getInteger("facing"));
        lastProgress = progress = compound.getFloat("progress");
        extending = compound.getBoolean("extending");
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("blockId", Block.getIdFromBlock(pistonState.getBlock()));
        compound.setInteger("blockData", pistonState.getBlock().getMetaFromState(pistonState));
        compound.setInteger("facing", pistonFacing.getIndex());
        compound.setFloat("progress", lastProgress);
        compound.setBoolean("extending", extending);
    }
}
