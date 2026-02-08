package net.minecraft.entity.ai;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ParticleTypes;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class EntityAIMate extends EntityAIBase {
    World theWorld;
    int spawnBabyDelay;
    double moveSpeed;
    private final EntityAnimal theAnimal;
    private EntityAnimal targetMate;

    public EntityAIMate(EntityAnimal animal, double speedIn) {
        theAnimal = animal;
        theWorld = animal.worldObj;
        moveSpeed = speedIn;
        setMutexBits(3);
    }

    public boolean shouldExecute() {
        if (!theAnimal.isInLove()) {
            return false;
        } else {
            targetMate = getNearbyMate();
            return targetMate != null;
        }
    }

    public boolean continueExecuting() {
        return targetMate.isEntityAlive() && targetMate.isInLove() && spawnBabyDelay < 60;
    }

    public void resetTask() {
        targetMate = null;
        spawnBabyDelay = 0;
    }

    public void updateTask() {
        theAnimal.getLookHelper().setLookPositionWithEntity(targetMate, 10.0F, (float) theAnimal.getVerticalFaceSpeed());
        theAnimal.getNavigator().tryMoveToEntityLiving(targetMate, moveSpeed);
        ++spawnBabyDelay;

        if (spawnBabyDelay >= 60 && theAnimal.getDistanceSqToEntity(targetMate) < 9.0D) {
            spawnBaby();
        }
    }

    private EntityAnimal getNearbyMate() {
        float f = 8.0F;
        List<EntityAnimal> list = theWorld.getEntitiesWithinAABB(theAnimal.getClass(), theAnimal.getEntityBoundingBox().expand(f, f, f));
        double d0 = Double.MAX_VALUE;
        EntityAnimal entityanimal = null;

        for (EntityAnimal entityanimal1 : list) {
            if (theAnimal.canMateWith(entityanimal1) && theAnimal.getDistanceSqToEntity(entityanimal1) < d0) {
                entityanimal = entityanimal1;
                d0 = theAnimal.getDistanceSqToEntity(entityanimal1);
            }
        }

        return entityanimal;
    }

    private void spawnBaby() {
        EntityAgeable entityageable = theAnimal.createChild(targetMate);

        if (entityageable != null) {
            EntityPlayer entityplayer = theAnimal.getPlayerInLove();

            if (entityplayer == null && targetMate.getPlayerInLove() != null) {
                entityplayer = targetMate.getPlayerInLove();
            }

            if (entityplayer != null) {
                entityplayer.triggerAchievement(StatList.animalsBredStat);

                if (theAnimal instanceof EntityCow) {
                    entityplayer.triggerAchievement(AchievementList.breedCow);
                }
            }

            theAnimal.setGrowingAge(6000);
            targetMate.setGrowingAge(6000);
            theAnimal.resetInLove();
            targetMate.resetInLove();
            entityageable.setGrowingAge(-24000);
            entityageable.setLocationAndAngles(theAnimal.posX, theAnimal.posY, theAnimal.posZ, 0.0F, 0.0F);
            theWorld.spawnEntityInWorld(entityageable);
            Random random = theAnimal.getRNG();

            for (int i = 0; i < 7; ++i) {
                double d0 = random.nextGaussian() * 0.02D;
                double d1 = random.nextGaussian() * 0.02D;
                double d2 = random.nextGaussian() * 0.02D;
                double d3 = random.nextDouble() * (double) theAnimal.width * 2.0D - (double) theAnimal.width;
                double d4 = 0.5D + random.nextDouble() * (double) theAnimal.height;
                double d5 = random.nextDouble() * (double) theAnimal.width * 2.0D - (double) theAnimal.width;
                theWorld.spawnParticle(ParticleTypes.HEART, theAnimal.posX + d3, theAnimal.posY + d4, theAnimal.posZ + d5, d0, d1, d2);
            }

            if (theWorld.getGameRules().getBoolean("doMobLoot")) {
                theWorld.spawnEntityInWorld(new EntityXPOrb(theWorld, theAnimal.posX, theAnimal.posY, theAnimal.posZ, random.nextInt(7) + 1));
            }
        }
    }
}
