package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAIOwnerHurtTarget extends EntityAITarget {
    EntityTameable theEntityTameable;
    EntityLivingBase theTarget;
    private int field_142050_e;

    public EntityAIOwnerHurtTarget(EntityTameable theEntityTameableIn) {
        super(theEntityTameableIn, false);
        theEntityTameable = theEntityTameableIn;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (!theEntityTameable.isTamed()) {
            return false;
        } else {
            EntityLivingBase entitylivingbase = theEntityTameable.getOwner();

            if (entitylivingbase == null) {
                return false;
            } else {
                theTarget = entitylivingbase.getLastAttacker();
                int i = entitylivingbase.getLastAttackerTime();
                return i != field_142050_e && isSuitableTarget(theTarget, false) && theEntityTameable.shouldAttackEntity(theTarget, entitylivingbase);
            }
        }
    }

    public void startExecuting() {
        taskOwner.setAttackTarget(theTarget);
        EntityLivingBase entitylivingbase = theEntityTameable.getOwner();

        if (entitylivingbase != null) {
            field_142050_e = entitylivingbase.getLastAttackerTime();
        }

        super.startExecuting();
    }
}
