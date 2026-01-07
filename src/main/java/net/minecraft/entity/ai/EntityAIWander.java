package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.Vec3;

public class EntityAIWander extends EntityAIBase {
    private final EntityCreature entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private final double speed;
    private int executionChance;
    private boolean mustUpdate;

    public EntityAIWander(EntityCreature creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public EntityAIWander(EntityCreature creatureIn, double speedIn, int chance) {
        entity = creatureIn;
        speed = speedIn;
        executionChance = chance;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (!mustUpdate) {
            if (entity.getAge() >= 100) {
                return false;
            }

            if (entity.getRNG().nextInt(executionChance) != 0) {
                return false;
            }
        }

        Vec3 vec3 = RandomPositionGenerator.findRandomTarget(entity, 10, 7);

        if (vec3 == null) {
            return false;
        } else {
            xPosition = vec3.xCoord();
            yPosition = vec3.yCoord();
            zPosition = vec3.zCoord();
            mustUpdate = false;
            return true;
        }
    }

    public boolean continueExecuting() {
        return !entity.getNavigator().noPath();
    }

    public void startExecuting() {
        entity.getNavigator().tryMoveToXYZ(xPosition, yPosition, zPosition, speed);
    }

    public void makeUpdate() {
        mustUpdate = true;
    }

    public void setExecutionChance(int newchance) {
        executionChance = newchance;
    }
}
