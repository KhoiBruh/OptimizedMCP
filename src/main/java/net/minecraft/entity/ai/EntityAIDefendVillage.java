package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.village.Village;

public class EntityAIDefendVillage extends EntityAITarget {
    EntityIronGolem irongolem;
    EntityLivingBase villageAgressorTarget;

    public EntityAIDefendVillage(EntityIronGolem ironGolemIn) {
        super(ironGolemIn, false, true);
        irongolem = ironGolemIn;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        Village village = irongolem.getVillage();

        if (village == null) {
            return false;
        } else {
            villageAgressorTarget = village.findNearestVillageAggressor(irongolem);

            if (villageAgressorTarget instanceof EntityCreeper) {
                return false;
            } else if (!isSuitableTarget(villageAgressorTarget, false)) {
                if (taskOwner.getRNG().nextInt(20) == 0) {
                    villageAgressorTarget = village.getNearestTargetPlayer(irongolem);
                    return isSuitableTarget(villageAgressorTarget, false);
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public void startExecuting() {
        irongolem.setAttackTarget(villageAgressorTarget);
        super.startExecuting();
    }
}
