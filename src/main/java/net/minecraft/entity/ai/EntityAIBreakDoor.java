package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumDifficulty;

public class EntityAIBreakDoor extends EntityAIDoorInteract {
    private int breakingTime;
    private int previousBreakProgress = -1;

    public EntityAIBreakDoor(EntityLiving entityIn) {
        super(entityIn);
    }

    public boolean shouldExecute() {
        if (!super.shouldExecute()) {
            return false;
        } else if (!theEntity.worldObj.getGameRules().getBoolean("mobGriefing")) {
            return false;
        } else {
            BlockDoor blockdoor = doorBlock;
            return !BlockDoor.isOpen(theEntity.worldObj, doorPosition);
        }
    }

    public void startExecuting() {
        super.startExecuting();
        breakingTime = 0;
    }

    public boolean continueExecuting() {
        double d0 = theEntity.getDistanceSq(doorPosition);
        boolean flag;

        if (breakingTime <= 240) {
            BlockDoor blockdoor = doorBlock;

            if (!BlockDoor.isOpen(theEntity.worldObj, doorPosition) && d0 < 4.0D) {
                flag = true;
                return true;
            }
        }

        flag = false;
        return false;
    }

    public void resetTask() {
        super.resetTask();
        theEntity.worldObj.sendBlockBreakProgress(theEntity.getEntityId(), doorPosition, -1);
    }

    public void updateTask() {
        super.updateTask();

        if (theEntity.getRNG().nextInt(20) == 0) {
            theEntity.worldObj.playAuxSFX(1010, doorPosition, 0);
        }

        ++breakingTime;
        int i = (int) ((float) breakingTime / 240.0F * 10.0F);

        if (i != previousBreakProgress) {
            theEntity.worldObj.sendBlockBreakProgress(theEntity.getEntityId(), doorPosition, i);
            previousBreakProgress = i;
        }

        if (breakingTime == 240 && theEntity.worldObj.getDifficulty() == EnumDifficulty.HARD) {
            theEntity.worldObj.setBlockToAir(doorPosition);
            theEntity.worldObj.playAuxSFX(1012, doorPosition, 0);
            theEntity.worldObj.playAuxSFX(2001, doorPosition, Block.getIdFromBlock(doorBlock));
        }
    }
}
