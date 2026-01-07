package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAIRestrictSun extends EntityAIBase {
    private final EntityCreature theEntity;

    public EntityAIRestrictSun(EntityCreature creature) {
        theEntity = creature;
    }

    public boolean shouldExecute() {
        return theEntity.worldObj.isDaytime();
    }

    public void startExecuting() {
        ((PathNavigateGround) theEntity.getNavigator()).setAvoidSun(true);
    }

    public void resetTask() {
        ((PathNavigateGround) theEntity.getNavigator()).setAvoidSun(false);
    }
}
