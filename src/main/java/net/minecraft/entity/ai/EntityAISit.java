package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAISit extends EntityAIBase {
    private final EntityTameable theEntity;
    private boolean isSitting;

    public EntityAISit(EntityTameable entityIn) {
        theEntity = entityIn;
        setMutexBits(5);
    }

    public boolean shouldExecute() {
        if (!theEntity.isTamed()) {
            return false;
        } else if (theEntity.isInWater()) {
            return false;
        } else if (!theEntity.onGround) {
            return false;
        } else {
            EntityLivingBase entitylivingbase = theEntity.getOwner();
            return entitylivingbase == null || ((!(theEntity.getDistanceSqToEntity(entitylivingbase) < 144.0D) || entitylivingbase.getAITarget() == null) && isSitting);
        }
    }

    public void startExecuting() {
        theEntity.getNavigator().clearPathEntity();
        theEntity.setSitting(true);
    }

    public void resetTask() {
        theEntity.setSitting(false);
    }

    public void setSitting(boolean sitting) {
        isSitting = sitting;
    }
}
