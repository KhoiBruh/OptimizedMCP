package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;

public class EntityAICreeperSwell extends EntityAIBase {
    EntityCreeper swellingCreeper;
    EntityLivingBase creeperAttackTarget;

    public EntityAICreeperSwell(EntityCreeper entitycreeperIn) {
        swellingCreeper = entitycreeperIn;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = swellingCreeper.getAttackTarget();
        return swellingCreeper.getCreeperState() > 0 || entitylivingbase != null && swellingCreeper.getDistanceSqToEntity(entitylivingbase) < 9.0D;
    }

    public void startExecuting() {
        swellingCreeper.getNavigator().clearPathEntity();
        creeperAttackTarget = swellingCreeper.getAttackTarget();
    }

    public void resetTask() {
        creeperAttackTarget = null;
    }

    public void updateTask() {
        if (creeperAttackTarget == null) {
            swellingCreeper.setCreeperState(-1);
        } else if (swellingCreeper.getDistanceSqToEntity(creeperAttackTarget) > 49.0D) {
            swellingCreeper.setCreeperState(-1);
        } else if (!swellingCreeper.getEntitySenses().canSee(creeperAttackTarget)) {
            swellingCreeper.setCreeperState(-1);
        } else {
            swellingCreeper.setCreeperState(1);
        }
    }
}
