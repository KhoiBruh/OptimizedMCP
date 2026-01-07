package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityAnimal;

import java.util.List;

public class EntityAIFollowParent extends EntityAIBase {
    EntityAnimal childAnimal;
    EntityAnimal parentAnimal;
    double moveSpeed;
    private int delayCounter;

    public EntityAIFollowParent(EntityAnimal animal, double speed) {
        childAnimal = animal;
        moveSpeed = speed;
    }

    public boolean shouldExecute() {
        if (childAnimal.getGrowingAge() >= 0) {
            return false;
        } else {
            List<EntityAnimal> list = childAnimal.worldObj.getEntitiesWithinAABB(childAnimal.getClass(), childAnimal.getEntityBoundingBox().expand(8.0D, 4.0D, 8.0D));
            EntityAnimal entityanimal = null;
            double d0 = Double.MAX_VALUE;

            for (EntityAnimal entityanimal1 : list) {
                if (entityanimal1.getGrowingAge() >= 0) {
                    double d1 = childAnimal.getDistanceSqToEntity(entityanimal1);

                    if (d1 <= d0) {
                        d0 = d1;
                        entityanimal = entityanimal1;
                    }
                }
            }

            if (entityanimal == null) {
                return false;
            } else if (d0 < 9.0D) {
                return false;
            } else {
                parentAnimal = entityanimal;
                return true;
            }
        }
    }

    public boolean continueExecuting() {
        if (childAnimal.getGrowingAge() >= 0) {
            return false;
        } else if (!parentAnimal.isEntityAlive()) {
            return false;
        } else {
            double d0 = childAnimal.getDistanceSqToEntity(parentAnimal);
            return d0 >= 9.0D && d0 <= 256.0D;
        }
    }

    public void startExecuting() {
        delayCounter = 0;
    }

    public void resetTask() {
        parentAnimal = null;
    }

    public void updateTask() {
        if (--delayCounter <= 0) {
            delayCounter = 10;
            childAnimal.getNavigator().tryMoveToEntityLiving(parentAnimal, moveSpeed);
        }
    }
}
