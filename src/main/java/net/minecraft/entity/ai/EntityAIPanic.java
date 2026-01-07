package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.Vec3;

public class EntityAIPanic extends EntityAIBase {
    protected double speed;
    private final EntityCreature theEntityCreature;
    private double randPosX;
    private double randPosY;
    private double randPosZ;

    public EntityAIPanic(EntityCreature creature, double speedIn) {
        theEntityCreature = creature;
        speed = speedIn;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (theEntityCreature.getAITarget() == null && !theEntityCreature.isBurning()) {
            return false;
        } else {
            Vec3 vec3 = RandomPositionGenerator.findRandomTarget(theEntityCreature, 5, 4);

            if (vec3 == null) {
                return false;
            } else {
                randPosX = vec3.xCoord();
                randPosY = vec3.yCoord();
                randPosZ = vec3.zCoord();
                return true;
            }
        }
    }

    public void startExecuting() {
        theEntityCreature.getNavigator().tryMoveToXYZ(randPosX, randPosY, randPosZ, speed);
    }

    public boolean continueExecuting() {
        return !theEntityCreature.getNavigator().noPath();
    }
}
