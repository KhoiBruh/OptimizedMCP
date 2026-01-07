package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityAILookIdle extends EntityAIBase {
    private final EntityLiving idleEntity;
    private double lookX;
    private double lookZ;
    private int idleTime;

    public EntityAILookIdle(EntityLiving entitylivingIn) {
        idleEntity = entitylivingIn;
        setMutexBits(3);
    }

    public boolean shouldExecute() {
        return idleEntity.getRNG().nextFloat() < 0.02F;
    }

    public boolean continueExecuting() {
        return idleTime >= 0;
    }

    public void startExecuting() {
        double d0 = (Math.PI * 2D) * idleEntity.getRNG().nextDouble();
        lookX = Math.cos(d0);
        lookZ = Math.sin(d0);
        idleTime = 20 + idleEntity.getRNG().nextInt(20);
    }

    public void updateTask() {
        --idleTime;
        idleEntity.getLookHelper().setLookPosition(idleEntity.posX + lookX, idleEntity.posY + (double) idleEntity.getEyeHeight(), idleEntity.posZ + lookZ, 10.0F, (float) idleEntity.getVerticalFaceSpeed());
    }
}
