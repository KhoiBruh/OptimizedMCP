package net.minecraft.entity.monster;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.Random;

public class EntityGhast extends EntityFlying implements IMob {
    private int explosionStrength = 1;

    public EntityGhast(World worldIn) {
        super(worldIn);
        setSize(4.0F, 4.0F);
        isImmuneToFire = true;
        experienceValue = 5;
        moveHelper = new EntityGhast.GhastMoveHelper(this);
        tasks.addTask(5, new EntityGhast.AIRandomFly(this));
        tasks.addTask(7, new EntityGhast.AILookAround(this));
        tasks.addTask(7, new EntityGhast.AIFireballAttack(this));
        targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
    }

    public boolean isAttacking() {
        return dataWatcher.getWatchableObjectByte(16) != 0;
    }

    public void setAttacking(boolean attacking) {
        dataWatcher.updateObject(16, (byte) (attacking ? 1 : 0));
    }

    public int getFireballStrength() {
        return explosionStrength;
    }

    public void onUpdate() {
        super.onUpdate();

        if (!worldObj.isRemote && worldObj.getDifficulty() == Difficulty.PEACEFUL) {
            setDead();
        }
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else if ("fireball".equals(source.getDamageType()) && source.getEntity() instanceof EntityPlayer) {
            super.attackEntityFrom(source, 1000.0F);
            ((EntityPlayer) source.getEntity()).triggerAchievement(AchievementList.ghast);
            return true;
        } else {
            return super.attackEntityFrom(source, amount);
        }
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(100.0D);
    }

    protected String getLivingSound() {
        return "mob.ghast.moan";
    }

    protected String getHurtSound() {
        return "mob.ghast.scream";
    }

    protected String getDeathSound() {
        return "mob.ghast.death";
    }

    protected Item getDropItem() {
        return Items.gunpowder;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int i = rand.nextInt(2) + rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j) {
            dropItem(Items.ghast_tear, 1);
        }

        i = rand.nextInt(3) + rand.nextInt(1 + lootingModifier);

        for (int k = 0; k < i; ++k) {
            dropItem(Items.gunpowder, 1);
        }
    }

    protected float getSoundVolume() {
        return 10.0F;
    }

    public boolean getCanSpawnHere() {
        return rand.nextInt(20) == 0 && super.getCanSpawnHere() && worldObj.getDifficulty() != Difficulty.PEACEFUL;
    }

    public int getMaxSpawnedInChunk() {
        return 1;
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("ExplosionPower", explosionStrength);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("ExplosionPower", 99)) {
            explosionStrength = tagCompund.getInteger("ExplosionPower");
        }
    }

    public float getEyeHeight() {
        return 2.6F;
    }

    static class AIFireballAttack extends EntityAIBase {
        public int attackTimer;
        private final EntityGhast parentEntity;

        public AIFireballAttack(EntityGhast ghast) {
            parentEntity = ghast;
        }

        public boolean shouldExecute() {
            return parentEntity.getAttackTarget() != null;
        }

        public void startExecuting() {
            attackTimer = 0;
        }

        public void resetTask() {
            parentEntity.setAttacking(false);
        }

        public void updateTask() {
            EntityLivingBase entitylivingbase = parentEntity.getAttackTarget();
            double d0 = 64.0D;

            if (entitylivingbase.getDistanceSqToEntity(parentEntity) < d0 * d0 && parentEntity.canEntityBeSeen(entitylivingbase)) {
                World world = parentEntity.worldObj;
                ++attackTimer;

                if (attackTimer == 10) {
                    world.playAuxSFXAtEntity(null, 1007, new BlockPos(parentEntity), 0);
                }

                if (attackTimer == 20) {
                    double d1 = 4.0D;
                    Vec3 vec3 = parentEntity.getLook(1.0F);
                    double d2 = entitylivingbase.posX - (parentEntity.posX + vec3.xCoord() * d1);
                    double d3 = entitylivingbase.getEntityBoundingBox().minY + (double) (entitylivingbase.height / 2.0F) - (0.5D + parentEntity.posY + (double) (parentEntity.height / 2.0F));
                    double d4 = entitylivingbase.posZ - (parentEntity.posZ + vec3.zCoord() * d1);
                    world.playAuxSFXAtEntity(null, 1008, new BlockPos(parentEntity), 0);
                    EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, parentEntity, d2, d3, d4);
                    entitylargefireball.explosionPower = parentEntity.getFireballStrength();
                    entitylargefireball.posX = parentEntity.posX + vec3.xCoord() * d1;
                    entitylargefireball.posY = parentEntity.posY + (double) (parentEntity.height / 2.0F) + 0.5D;
                    entitylargefireball.posZ = parentEntity.posZ + vec3.zCoord() * d1;
                    world.spawnEntityInWorld(entitylargefireball);
                    attackTimer = -40;
                }
            } else if (attackTimer > 0) {
                --attackTimer;
            }

            parentEntity.setAttacking(attackTimer > 10);
        }
    }

    static class AILookAround extends EntityAIBase {
        private final EntityGhast parentEntity;

        public AILookAround(EntityGhast ghast) {
            parentEntity = ghast;
            setMutexBits(2);
        }

        public boolean shouldExecute() {
            return true;
        }

        public void updateTask() {
            if (parentEntity.getAttackTarget() == null) {
                parentEntity.renderYawOffset = parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * 180.0F / (float) Math.PI;
            } else {
                EntityLivingBase entitylivingbase = parentEntity.getAttackTarget();
                double d0 = 64.0D;

                if (entitylivingbase.getDistanceSqToEntity(parentEntity) < d0 * d0) {
                    double d1 = entitylivingbase.posX - parentEntity.posX;
                    double d2 = entitylivingbase.posZ - parentEntity.posZ;
                    parentEntity.renderYawOffset = parentEntity.rotationYaw = -((float) MathHelper.atan2(d1, d2)) * 180.0F / (float) Math.PI;
                }
            }
        }
    }

    static class AIRandomFly extends EntityAIBase {
        private final EntityGhast parentEntity;

        public AIRandomFly(EntityGhast ghast) {
            parentEntity = ghast;
            setMutexBits(1);
        }

        public boolean shouldExecute() {
            EntityMoveHelper entitymovehelper = parentEntity.getMoveHelper();

            if (!entitymovehelper.isUpdating()) {
                return true;
            } else {
                double d0 = entitymovehelper.getX() - parentEntity.posX;
                double d1 = entitymovehelper.getY() - parentEntity.posY;
                double d2 = entitymovehelper.getZ() - parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        public boolean continueExecuting() {
            return false;
        }

        public void startExecuting() {
            Random random = parentEntity.getRNG();
            double d0 = parentEntity.posX + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = parentEntity.posY + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = parentEntity.posZ + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
        }
    }

    static class GhastMoveHelper extends EntityMoveHelper {
        private final EntityGhast parentEntity;
        private int courseChangeCooldown;

        public GhastMoveHelper(EntityGhast ghast) {
            super(ghast);
            parentEntity = ghast;
        }

        public void onUpdateMoveHelper() {
            if (update) {
                double d0 = posX - parentEntity.posX;
                double d1 = posY - parentEntity.posY;
                double d2 = posZ - parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (courseChangeCooldown-- <= 0) {
                    courseChangeCooldown += parentEntity.getRNG().nextInt(5) + 2;
                    d3 = MathHelper.sqrt_double(d3);

                    if (isNotColliding(posX, posY, posZ, d3)) {
                        parentEntity.motionX += d0 / d3 * 0.1D;
                        parentEntity.motionY += d1 / d3 * 0.1D;
                        parentEntity.motionZ += d2 / d3 * 0.1D;
                    } else {
                        update = false;
                    }
                }
            }
        }

        private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
            double d0 = (x - parentEntity.posX) / p_179926_7_;
            double d1 = (y - parentEntity.posY) / p_179926_7_;
            double d2 = (z - parentEntity.posZ) / p_179926_7_;
            AxisAlignedBB axisalignedbb = parentEntity.getEntityBoundingBox();

            for (int i = 1; (double) i < p_179926_7_; ++i) {
                axisalignedbb = axisalignedbb.offset(d0, d1, d2);

                if (!parentEntity.worldObj.getCollidingBoundingBoxes(parentEntity, axisalignedbb).isEmpty()) {
                    return false;
                }
            }

            return true;
        }
    }
}
