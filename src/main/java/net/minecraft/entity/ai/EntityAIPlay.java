package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.Vec3;

import java.util.List;

public class EntityAIPlay extends EntityAIBase {
    private final EntityVillager villagerObj;
    private EntityLivingBase targetVillager;
    private final double speed;
    private int playTime;

    public EntityAIPlay(EntityVillager villagerObjIn, double speedIn) {
        villagerObj = villagerObjIn;
        speed = speedIn;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villagerObj.getGrowingAge() >= 0) {
            return false;
        } else if (villagerObj.getRNG().nextInt(400) != 0) {
            return false;
        } else {
            List<EntityVillager> list = villagerObj.worldObj.getEntitiesWithinAABB(EntityVillager.class, villagerObj.getEntityBoundingBox().expand(6.0D, 3.0D, 6.0D));
            double d0 = Double.MAX_VALUE;

            for (EntityVillager entityvillager : list) {
                if (entityvillager != villagerObj && !entityvillager.isPlaying() && entityvillager.getGrowingAge() < 0) {
                    double d1 = entityvillager.getDistanceSqToEntity(villagerObj);

                    if (d1 <= d0) {
                        d0 = d1;
                        targetVillager = entityvillager;
                    }
                }
            }

            if (targetVillager == null) {
                Vec3 vec3 = RandomPositionGenerator.findRandomTarget(villagerObj, 16, 3);

                return vec3 != null;
            }

            return true;
        }
    }

    public boolean continueExecuting() {
        return playTime > 0;
    }

    public void startExecuting() {
        if (targetVillager != null) {
            villagerObj.setPlaying(true);
        }

        playTime = 1000;
    }

    public void resetTask() {
        villagerObj.setPlaying(false);
        targetVillager = null;
    }

    public void updateTask() {
        --playTime;

        if (targetVillager != null) {
            if (villagerObj.getDistanceSqToEntity(targetVillager) > 4.0D) {
                villagerObj.getNavigator().tryMoveToEntityLiving(targetVillager, speed);
            }
        } else if (villagerObj.getNavigator().noPath()) {
            Vec3 vec3 = RandomPositionGenerator.findRandomTarget(villagerObj, 16, 3);

            if (vec3 == null) {
                return;
            }

            villagerObj.getNavigator().tryMoveToXYZ(vec3.xCoord(), vec3.yCoord(), vec3.zCoord(), speed);
        }
    }
}
