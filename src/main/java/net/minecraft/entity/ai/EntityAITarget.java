package net.minecraft.entity.ai;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

public abstract class EntityAITarget extends EntityAIBase {
    protected final EntityCreature taskOwner;
    protected boolean shouldCheckSight;
    private final boolean nearbyOnly;
    private int targetSearchStatus;
    private int targetSearchDelay;
    private int targetUnseenTicks;

    public EntityAITarget(EntityCreature creature, boolean checkSight) {
        this(creature, checkSight, false);
    }

    public EntityAITarget(EntityCreature creature, boolean checkSight, boolean onlyNearby) {
        taskOwner = creature;
        shouldCheckSight = checkSight;
        nearbyOnly = onlyNearby;
    }

    public static boolean isSuitableTarget(EntityLiving attacker, EntityLivingBase target, boolean includeInvincibles, boolean checkSight) {
        if (target == null) {
            return false;
        } else if (target == attacker) {
            return false;
        } else if (!target.isEntityAlive()) {
            return false;
        } else if (!attacker.canAttackClass(target.getClass())) {
            return false;
        } else {
            Team team = attacker.getTeam();
            Team team1 = target.getTeam();

            if (team != null && team1 == team) {
                return false;
            } else {
                if (attacker instanceof IEntityOwnable && StringUtils.isNotEmpty(((IEntityOwnable) attacker).getOwnerId())) {
                    if (target instanceof IEntityOwnable && ((IEntityOwnable) attacker).getOwnerId().equals(((IEntityOwnable) target).getOwnerId())) {
                        return false;
                    }

                    if (target == ((IEntityOwnable) attacker).getOwner()) {
                        return false;
                    }
                } else if (target instanceof EntityPlayer && !includeInvincibles && ((EntityPlayer) target).capabilities.disableDamage) {
                    return false;
                }

                return !checkSight || attacker.getEntitySenses().canSee(target);
            }
        }
    }

    public boolean continueExecuting() {
        EntityLivingBase entitylivingbase = taskOwner.getAttackTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isEntityAlive()) {
            return false;
        } else {
            Team team = taskOwner.getTeam();
            Team team1 = entitylivingbase.getTeam();

            if (team != null && team1 == team) {
                return false;
            } else {
                double d0 = getTargetDistance();

                if (taskOwner.getDistanceSqToEntity(entitylivingbase) > d0 * d0) {
                    return false;
                } else {
                    if (shouldCheckSight) {
                        if (taskOwner.getEntitySenses().canSee(entitylivingbase)) {
                            targetUnseenTicks = 0;
                        } else if (++targetUnseenTicks > 60) {
                            return false;
                        }
                    }

                    return !(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer) entitylivingbase).capabilities.disableDamage;
                }
            }
        }
    }

    protected double getTargetDistance() {
        IAttributeInstance iattributeinstance = taskOwner.getEntityAttribute(SharedMonsterAttributes.followRange);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }

    public void startExecuting() {
        targetSearchStatus = 0;
        targetSearchDelay = 0;
        targetUnseenTicks = 0;
    }

    public void resetTask() {
        taskOwner.setAttackTarget(null);
    }

    protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
        if (!isSuitableTarget(taskOwner, target, includeInvincibles, shouldCheckSight)) {
            return false;
        } else if (!taskOwner.isWithinHomeDistanceFromPosition(new BlockPos(target))) {
            return false;
        } else {
            if (nearbyOnly) {
                if (--targetSearchDelay <= 0) {
                    targetSearchStatus = 0;
                }

                if (targetSearchStatus == 0) {
                    targetSearchStatus = canEasilyReach(target) ? 1 : 2;
                }

                return targetSearchStatus != 2;
            }

            return true;
        }
    }

    private boolean canEasilyReach(EntityLivingBase target) {
        targetSearchDelay = 10 + taskOwner.getRNG().nextInt(5);
        PathEntity pathentity = taskOwner.getNavigator().getPathToEntityLiving(target);

        if (pathentity == null) {
            return false;
        } else {
            PathPoint pathpoint = pathentity.getFinalPathPoint();

            if (pathpoint == null) {
                return false;
            } else {
                int i = pathpoint.xCoord - MathHelper.floor_double(target.posX);
                int j = pathpoint.zCoord - MathHelper.floor_double(target.posZ);
                return (double) (i * i + j * j) <= 2.25D;
            }
        }
    }
}
