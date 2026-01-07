package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public class EntityAIMoveTowardsTarget extends EntityAIBase {
    private final EntityCreature theEntity;
    private EntityLivingBase targetEntity;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
    private final double speed;
    private final float maxTargetDistance;

    public EntityAIMoveTowardsTarget(EntityCreature creature, double speedIn, float targetMaxDistance) {
        theEntity = creature;
        speed = speedIn;
        maxTargetDistance = targetMaxDistance;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        targetEntity = theEntity.getAttackTarget();

        if (targetEntity == null) {
            return false;
        } else if (targetEntity.getDistanceSqToEntity(theEntity) > (double) (maxTargetDistance * maxTargetDistance)) {
            return false;
        } else {
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(theEntity, 16, 7, new Vec3(targetEntity.posX, targetEntity.posY, targetEntity.posZ));

            if (vec3 == null) {
                return false;
            } else {
                movePosX = vec3.xCoord();
                movePosY = vec3.yCoord();
                movePosZ = vec3.zCoord();
                return true;
            }
        }
    }

    public boolean continueExecuting() {
        return !theEntity.getNavigator().noPath() && targetEntity.isEntityAlive() && targetEntity.getDistanceSqToEntity(theEntity) < (double) (maxTargetDistance * maxTargetDistance);
    }

    public void resetTask() {
        targetEntity = null;
    }

    public void startExecuting() {
        theEntity.getNavigator().tryMoveToXYZ(movePosX, movePosY, movePosZ, speed);
    }
}
