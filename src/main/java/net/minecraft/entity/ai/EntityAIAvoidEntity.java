package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Vec3;

import java.util.List;

public class EntityAIAvoidEntity<T extends Entity> extends EntityAIBase {
    private final Predicate<Entity> canBeSeenSelector;
    protected EntityCreature theEntity;
    protected T closestLivingEntity;
    private final double farSpeed;
    private final double nearSpeed;
    private final float avoidDistance;
    private PathEntity entityPathEntity;
    private final PathNavigate entityPathNavigate;
    private final Class<T> classToAvoid;
    private final Predicate<? super T> avoidTargetSelector;

    public EntityAIAvoidEntity(EntityCreature theEntityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        this(theEntityIn, classToAvoidIn, Predicates.alwaysTrue(), avoidDistanceIn, farSpeedIn, nearSpeedIn);
    }

    public EntityAIAvoidEntity(EntityCreature theEntityIn, Class<T> classToAvoidIn, Predicate<? super T> avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        canBeSeenSelector = new Predicate<Entity>() {
            public boolean apply(Entity p_apply_1_) {
                return p_apply_1_.isEntityAlive() && theEntity.getEntitySenses().canSee(p_apply_1_);
            }
        };
        theEntity = theEntityIn;
        classToAvoid = classToAvoidIn;
        avoidTargetSelector = avoidTargetSelectorIn;
        avoidDistance = avoidDistanceIn;
        farSpeed = farSpeedIn;
        nearSpeed = nearSpeedIn;
        entityPathNavigate = theEntityIn.getNavigator();
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        List<T> list = theEntity.worldObj.getEntitiesWithinAABB(classToAvoid, theEntity.getEntityBoundingBox().expand(avoidDistance, 3.0D, avoidDistance), Predicates.and(EntitySelectors.NOT_SPECTATING, canBeSeenSelector, avoidTargetSelector));

        if (list.isEmpty()) {
            return false;
        } else {
            closestLivingEntity = list.get(0);
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(theEntity, 16, 7, new Vec3(closestLivingEntity.posX, closestLivingEntity.posY, closestLivingEntity.posZ));

            if (vec3 == null) {
                return false;
            } else if (closestLivingEntity.getDistanceSq(vec3.xCoord(), vec3.yCoord(), vec3.zCoord()) < closestLivingEntity.getDistanceSqToEntity(theEntity)) {
                return false;
            } else {
                entityPathEntity = entityPathNavigate.getPathToXYZ(vec3.xCoord(), vec3.yCoord(), vec3.zCoord());
                return entityPathEntity != null && entityPathEntity.isDestinationSame(vec3);
            }
        }
    }

    public boolean continueExecuting() {
        return !entityPathNavigate.noPath();
    }

    public void startExecuting() {
        entityPathNavigate.setPath(entityPathEntity, farSpeed);
    }

    public void resetTask() {
        closestLivingEntity = null;
    }

    public void updateTask() {
        if (theEntity.getDistanceSqToEntity(closestLivingEntity) < 49.0D) {
            theEntity.getNavigator().setSpeed(nearSpeed);
        } else {
            theEntity.getNavigator().setSpeed(farSpeed);
        }
    }
}
