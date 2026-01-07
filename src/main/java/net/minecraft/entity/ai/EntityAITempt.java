package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAITempt extends EntityAIBase {
    private final EntityCreature temptedEntity;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    private EntityPlayer temptingPlayer;
    private int delayTemptCounter;
    private boolean isRunning;
    private final Item temptItem;
    private final boolean scaredByPlayerMovement;
    private boolean avoidWater;

    public EntityAITempt(EntityCreature temptedEntityIn, double speedIn, Item temptItemIn, boolean scaredByPlayerMovementIn) {
        temptedEntity = temptedEntityIn;
        speed = speedIn;
        temptItem = temptItemIn;
        scaredByPlayerMovement = scaredByPlayerMovementIn;
        setMutexBits(3);

        if (!(temptedEntityIn.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
        }
    }

    public boolean shouldExecute() {
        if (delayTemptCounter > 0) {
            --delayTemptCounter;
            return false;
        } else {
            temptingPlayer = temptedEntity.worldObj.getClosestPlayerToEntity(temptedEntity, 10.0D);

            if (temptingPlayer == null) {
                return false;
            } else {
                ItemStack itemstack = temptingPlayer.getCurrentEquippedItem();
                return itemstack != null && itemstack.getItem() == temptItem;
            }
        }
    }

    public boolean continueExecuting() {
        if (scaredByPlayerMovement) {
            if (temptedEntity.getDistanceSqToEntity(temptingPlayer) < 36.0D) {
                if (temptingPlayer.getDistanceSq(targetX, targetY, targetZ) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double) temptingPlayer.rotationPitch - pitch) > 5.0D || Math.abs((double) temptingPlayer.rotationYaw - yaw) > 5.0D) {
                    return false;
                }
            } else {
                targetX = temptingPlayer.posX;
                targetY = temptingPlayer.posY;
                targetZ = temptingPlayer.posZ;
            }

            pitch = temptingPlayer.rotationPitch;
            yaw = temptingPlayer.rotationYaw;
        }

        return shouldExecute();
    }

    public void startExecuting() {
        targetX = temptingPlayer.posX;
        targetY = temptingPlayer.posY;
        targetZ = temptingPlayer.posZ;
        isRunning = true;
        avoidWater = ((PathNavigateGround) temptedEntity.getNavigator()).getAvoidsWater();
        ((PathNavigateGround) temptedEntity.getNavigator()).setAvoidsWater(false);
    }

    public void resetTask() {
        temptingPlayer = null;
        temptedEntity.getNavigator().clearPathEntity();
        delayTemptCounter = 100;
        isRunning = false;
        ((PathNavigateGround) temptedEntity.getNavigator()).setAvoidsWater(avoidWater);
    }

    public void updateTask() {
        temptedEntity.getLookHelper().setLookPositionWithEntity(temptingPlayer, 30.0F, (float) temptedEntity.getVerticalFaceSpeed());

        if (temptedEntity.getDistanceSqToEntity(temptingPlayer) < 6.25D) {
            temptedEntity.getNavigator().clearPathEntity();
        } else {
            temptedEntity.getNavigator().tryMoveToEntityLiving(temptingPlayer, speed);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
