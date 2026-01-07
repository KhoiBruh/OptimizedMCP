package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityAIAttackOnCollide extends EntityAIBase {
    protected EntityCreature attacker;
    World worldObj;
    int attackTick;
    double speedTowardsTarget;
    boolean longMemory;
    PathEntity entityPathEntity;
    Class<? extends Entity> classTarget;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;

    public EntityAIAttackOnCollide(EntityCreature creature, Class<? extends Entity> targetClass, double speedIn, boolean useLongMemory) {
        this(creature, speedIn, useLongMemory);
        classTarget = targetClass;
    }

    public EntityAIAttackOnCollide(EntityCreature creature, double speedIn, boolean useLongMemory) {
        attacker = creature;
        worldObj = creature.worldObj;
        speedTowardsTarget = speedIn;
        longMemory = useLongMemory;
        setMutexBits(3);
    }

    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = attacker.getAttackTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isEntityAlive()) {
            return false;
        } else if (classTarget != null && !classTarget.isAssignableFrom(entitylivingbase.getClass())) {
            return false;
        } else {
            entityPathEntity = attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
            return entityPathEntity != null;
        }
    }

    public boolean continueExecuting() {
        EntityLivingBase entitylivingbase = attacker.getAttackTarget();
        return entitylivingbase != null && (entitylivingbase.isEntityAlive() && (!longMemory ? !attacker.getNavigator().noPath() : attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))));
    }

    public void startExecuting() {
        attacker.getNavigator().setPath(entityPathEntity, speedTowardsTarget);
        delayCounter = 0;
    }

    public void resetTask() {
        attacker.getNavigator().clearPathEntity();
    }

    public void updateTask() {
        EntityLivingBase entitylivingbase = attacker.getAttackTarget();
        attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        double d0 = attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        double d1 = func_179512_a(entitylivingbase);
        --delayCounter;

        if ((longMemory || attacker.getEntitySenses().canSee(entitylivingbase)) && delayCounter <= 0 && (targetX == 0.0D && targetY == 0.0D && targetZ == 0.0D || entitylivingbase.getDistanceSq(targetX, targetY, targetZ) >= 1.0D || attacker.getRNG().nextFloat() < 0.05F)) {
            targetX = entitylivingbase.posX;
            targetY = entitylivingbase.getEntityBoundingBox().minY;
            targetZ = entitylivingbase.posZ;
            delayCounter = 4 + attacker.getRNG().nextInt(7);

            if (d0 > 1024.0D) {
                delayCounter += 10;
            } else if (d0 > 256.0D) {
                delayCounter += 5;
            }

            if (!attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, speedTowardsTarget)) {
                delayCounter += 15;
            }
        }

        attackTick = Math.max(attackTick - 1, 0);

        if (d0 <= d1 && attackTick == 0) {
            attackTick = 20;

            if (attacker.getHeldItem() != null) {
                attacker.swingItem();
            }

            attacker.attackEntityAsMob(entitylivingbase);
        }
    }

    protected double func_179512_a(EntityLivingBase attackTarget) {
        return attacker.width * 2.0F * attacker.width * 2.0F + attackTarget.width;
    }
}
