package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class EntityAIMoveToBlock extends EntityAIBase {
    private final EntityCreature theEntity;
    private final double movementSpeed;
    protected int runDelay;
    protected BlockPos destinationBlock = BlockPos.ORIGIN;
    private int timeoutCounter;
    private int field_179490_f;
    private boolean isAboveDestination;
    private final int searchLength;

    public EntityAIMoveToBlock(EntityCreature creature, double speedIn, int length) {
        theEntity = creature;
        movementSpeed = speedIn;
        searchLength = length;
        setMutexBits(5);
    }

    public boolean shouldExecute() {
        if (runDelay > 0) {
            --runDelay;
            return false;
        } else {
            runDelay = 200 + theEntity.getRNG().nextInt(200);
            return searchForDestination();
        }
    }

    public boolean continueExecuting() {
        return timeoutCounter >= -field_179490_f && timeoutCounter <= 1200 && shouldMoveTo(theEntity.worldObj, destinationBlock);
    }

    public void startExecuting() {
        theEntity.getNavigator().tryMoveToXYZ((double) ((float) destinationBlock.getX()) + 0.5D, destinationBlock.getY() + 1, (double) ((float) destinationBlock.getZ()) + 0.5D, movementSpeed);
        timeoutCounter = 0;
        field_179490_f = theEntity.getRNG().nextInt(theEntity.getRNG().nextInt(1200) + 1200) + 1200;
    }

    public void resetTask() {
    }

    public void updateTask() {
        if (theEntity.getDistanceSqToCenter(destinationBlock.up()) > 1.0D) {
            isAboveDestination = false;
            ++timeoutCounter;

            if (timeoutCounter % 40 == 0) {
                theEntity.getNavigator().tryMoveToXYZ((double) ((float) destinationBlock.getX()) + 0.5D, destinationBlock.getY() + 1, (double) ((float) destinationBlock.getZ()) + 0.5D, movementSpeed);
            }
        } else {
            isAboveDestination = true;
            --timeoutCounter;
        }
    }

    protected boolean getIsAboveDestination() {
        return isAboveDestination;
    }

    private boolean searchForDestination() {
        int j = 1;
        BlockPos blockpos = new BlockPos(theEntity);

        for (int k = 0; k <= 1; k = k > 0 ? -k : 1 - k) {
            for (int l = 0; l < searchLength; ++l) {
                for (int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for (int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        BlockPos blockpos1 = blockpos.add(i1, k - 1, j1);

                        if (theEntity.isWithinHomeDistanceFromPosition(blockpos1) && shouldMoveTo(theEntity.worldObj, blockpos1)) {
                            destinationBlock = blockpos1;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected abstract boolean shouldMoveTo(World worldIn, BlockPos pos);
}
