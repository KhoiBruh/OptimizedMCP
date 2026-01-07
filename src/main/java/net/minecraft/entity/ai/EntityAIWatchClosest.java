package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAIWatchClosest extends EntityAIBase {
    protected EntityLiving theWatcher;
    protected Entity closestEntity;
    protected float maxDistanceForPlayer;
    protected Class<? extends Entity> watchedClass;
    private int lookTime;
    private final float chance;

    public EntityAIWatchClosest(EntityLiving entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance) {
        theWatcher = entitylivingIn;
        watchedClass = watchTargetClass;
        maxDistanceForPlayer = maxDistance;
        chance = 0.02F;
        setMutexBits(2);
    }

    public EntityAIWatchClosest(EntityLiving entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance, float chanceIn) {
        theWatcher = entitylivingIn;
        watchedClass = watchTargetClass;
        maxDistanceForPlayer = maxDistance;
        chance = chanceIn;
        setMutexBits(2);
    }

    public boolean shouldExecute() {
        if (theWatcher.getRNG().nextFloat() >= chance) {
            return false;
        } else {
            if (theWatcher.getAttackTarget() != null) {
                closestEntity = theWatcher.getAttackTarget();
            }

            if (watchedClass == EntityPlayer.class) {
                closestEntity = theWatcher.worldObj.getClosestPlayerToEntity(theWatcher, maxDistanceForPlayer);
            } else {
                closestEntity = theWatcher.worldObj.findNearestEntityWithinAABB(watchedClass, theWatcher.getEntityBoundingBox().expand(maxDistanceForPlayer, 3.0D, maxDistanceForPlayer), theWatcher);
            }

            return closestEntity != null;
        }
    }

    public boolean continueExecuting() {
        return closestEntity.isEntityAlive() && (!(theWatcher.getDistanceSqToEntity(closestEntity) > (double) (maxDistanceForPlayer * maxDistanceForPlayer)) && lookTime > 0);
    }

    public void startExecuting() {
        lookTime = 40 + theWatcher.getRNG().nextInt(40);
    }

    public void resetTask() {
        closestEntity = null;
    }

    public void updateTask() {
        theWatcher.getLookHelper().setLookPosition(closestEntity.posX, closestEntity.posY + (double) closestEntity.getEyeHeight(), closestEntity.posZ, 10.0F, (float) theWatcher.getVerticalFaceSpeed());
        --lookTime;
    }
}
