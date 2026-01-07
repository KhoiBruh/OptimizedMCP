package net.minecraft.entity.ai;

import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;

public class EntityAILookAtVillager extends EntityAIBase {
    private final EntityIronGolem theGolem;
    private EntityVillager theVillager;
    private int lookTime;

    public EntityAILookAtVillager(EntityIronGolem theGolemIn) {
        theGolem = theGolemIn;
        setMutexBits(3);
    }

    public boolean shouldExecute() {
        if (!theGolem.worldObj.isDaytime()) {
            return false;
        } else if (theGolem.getRNG().nextInt(8000) != 0) {
            return false;
        } else {
            theVillager = (EntityVillager) theGolem.worldObj.findNearestEntityWithinAABB(EntityVillager.class, theGolem.getEntityBoundingBox().expand(6.0D, 2.0D, 6.0D), theGolem);
            return theVillager != null;
        }
    }

    public boolean continueExecuting() {
        return lookTime > 0;
    }

    public void startExecuting() {
        lookTime = 400;
        theGolem.setHoldingRose(true);
    }

    public void resetTask() {
        theGolem.setHoldingRose(false);
        theVillager = null;
    }

    public void updateTask() {
        theGolem.getLookHelper().setLookPositionWithEntity(theVillager, 30.0F, 30.0F);
        --lookTime;
    }
}
