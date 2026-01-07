package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class EntityAIRunAroundLikeCrazy extends EntityAIBase {
    private final EntityHorse horseHost;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;

    public EntityAIRunAroundLikeCrazy(EntityHorse horse, double speedIn) {
        horseHost = horse;
        speed = speedIn;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (!horseHost.isTame() && horseHost.riddenByEntity != null) {
            Vec3 vec3 = RandomPositionGenerator.findRandomTarget(horseHost, 5, 4);

            if (vec3 == null) {
                return false;
            } else {
                targetX = vec3.xCoord();
                targetY = vec3.yCoord();
                targetZ = vec3.zCoord();
                return true;
            }
        } else {
            return false;
        }
    }

    public void startExecuting() {
        horseHost.getNavigator().tryMoveToXYZ(targetX, targetY, targetZ, speed);
    }

    public boolean continueExecuting() {
        return !horseHost.getNavigator().noPath() && horseHost.riddenByEntity != null;
    }

    public void updateTask() {
        if (horseHost.getRNG().nextInt(50) == 0) {
            if (horseHost.riddenByEntity instanceof EntityPlayer) {
                int i = horseHost.getTemper();
                int j = horseHost.getMaxTemper();

                if (j > 0 && horseHost.getRNG().nextInt(j) < i) {
                    horseHost.setTamedBy((EntityPlayer) horseHost.riddenByEntity);
                    horseHost.worldObj.setEntityState(horseHost, (byte) 7);
                    return;
                }

                horseHost.increaseTemper(5);
            }

            horseHost.riddenByEntity.mountEntity(null);
            horseHost.riddenByEntity = null;
            horseHost.makeHorseRearWithSound();
            horseHost.worldObj.setEntityState(horseHost, (byte) 6);
        }
    }
}
