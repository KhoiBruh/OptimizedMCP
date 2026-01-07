package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBlaze extends EntityMob {
    private float heightOffset = 0.5F;
    private int heightOffsetUpdateTime;

    public EntityBlaze(World worldIn) {
        super(worldIn);
        isImmuneToFire = true;
        experienceValue = 10;
        tasks.addTask(4, new EntityBlaze.AIFireballAttack(this));
        tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        tasks.addTask(7, new EntityAIWander(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(48.0D);
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    protected String getLivingSound() {
        return "mob.blaze.breathe";
    }

    protected String getHurtSound() {
        return "mob.blaze.hit";
    }

    protected String getDeathSound() {
        return "mob.blaze.death";
    }

    public int getBrightnessForRender(float partialTicks) {
        return 15728880;
    }

    public float getBrightness(float partialTicks) {
        return 1.0F;
    }

    public void onLivingUpdate() {
        if (!onGround && motionY < 0.0D) {
            motionY *= 0.6D;
        }

        if (worldObj.isRemote) {
            if (rand.nextInt(24) == 0 && !isSilent()) {
                worldObj.playSound(posX + 0.5D, posY + 0.5D, posZ + 0.5D, "fire.fire", 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
            }

            for (int i = 0; i < 2; ++i) {
                worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX + (rand.nextDouble() - 0.5D) * (double) width, posY + rand.nextDouble() * (double) height, posZ + (rand.nextDouble() - 0.5D) * (double) width, 0.0D, 0.0D, 0.0D);
            }
        }

        super.onLivingUpdate();
    }

    protected void updateAITasks() {
        if (isWet()) {
            attackEntityFrom(DamageSource.drown, 1.0F);
        }

        --heightOffsetUpdateTime;

        if (heightOffsetUpdateTime <= 0) {
            heightOffsetUpdateTime = 100;
            heightOffset = 0.5F + (float) rand.nextGaussian() * 3.0F;
        }

        EntityLivingBase entitylivingbase = getAttackTarget();

        if (entitylivingbase != null && entitylivingbase.posY + (double) entitylivingbase.getEyeHeight() > posY + (double) getEyeHeight() + (double) heightOffset) {
            motionY += (0.30000001192092896D - motionY) * 0.30000001192092896D;
            isAirBorne = true;
        }

        super.updateAITasks();
    }

    public void fall(float distance, float damageMultiplier) {
    }

    protected Item getDropItem() {
        return Items.blaze_rod;
    }

    public boolean isBurning() {
        return func_70845_n();
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        if (wasRecentlyHit) {
            int i = rand.nextInt(2 + lootingModifier);

            for (int j = 0; j < i; ++j) {
                dropItem(Items.blaze_rod, 1);
            }
        }
    }

    public boolean func_70845_n() {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setOnFire(boolean onFire) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (onFire) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 = (byte) (b0 & -2);
        }

        dataWatcher.updateObject(16, b0);
    }

    protected boolean isValidLightLevel() {
        return true;
    }

    static class AIFireballAttack extends EntityAIBase {
        private final EntityBlaze blaze;
        private int field_179467_b;
        private int field_179468_c;

        public AIFireballAttack(EntityBlaze p_i45846_1_) {
            blaze = p_i45846_1_;
            setMutexBits(3);
        }

        public boolean shouldExecute() {
            EntityLivingBase entitylivingbase = blaze.getAttackTarget();
            return entitylivingbase != null && entitylivingbase.isEntityAlive();
        }

        public void startExecuting() {
            field_179467_b = 0;
        }

        public void resetTask() {
            blaze.setOnFire(false);
        }

        public void updateTask() {
            --field_179468_c;
            EntityLivingBase entitylivingbase = blaze.getAttackTarget();
            double d0 = blaze.getDistanceSqToEntity(entitylivingbase);

            if (d0 < 4.0D) {
                if (field_179468_c <= 0) {
                    field_179468_c = 20;
                    blaze.attackEntityAsMob(entitylivingbase);
                }

                blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, 1.0D);
            } else if (d0 < 256.0D) {
                double d1 = entitylivingbase.posX - blaze.posX;
                double d2 = entitylivingbase.getEntityBoundingBox().minY + (double) (entitylivingbase.height / 2.0F) - (blaze.posY + (double) (blaze.height / 2.0F));
                double d3 = entitylivingbase.posZ - blaze.posZ;

                if (field_179468_c <= 0) {
                    ++field_179467_b;

                    if (field_179467_b == 1) {
                        field_179468_c = 60;
                        blaze.setOnFire(true);
                    } else if (field_179467_b <= 4) {
                        field_179468_c = 6;
                    } else {
                        field_179468_c = 100;
                        field_179467_b = 0;
                        blaze.setOnFire(false);
                    }

                    if (field_179467_b > 1) {
                        float f = MathHelper.sqrt_float(MathHelper.sqrt_double(d0)) * 0.5F;
                        blaze.worldObj.playAuxSFXAtEntity(null, 1009, new BlockPos((int) blaze.posX, (int) blaze.posY, (int) blaze.posZ), 0);

                        for (int i = 0; i < 1; ++i) {
                            EntitySmallFireball entitysmallfireball = new EntitySmallFireball(blaze.worldObj, blaze, d1 + blaze.getRNG().nextGaussian() * (double) f, d2, d3 + blaze.getRNG().nextGaussian() * (double) f);
                            entitysmallfireball.posY = blaze.posY + (double) (blaze.height / 2.0F) + 0.5D;
                            blaze.worldObj.spawnEntityInWorld(entitysmallfireball);
                        }
                    }
                }

                blaze.getLookHelper().setLookPositionWithEntity(entitylivingbase, 10.0F, 10.0F);
            } else {
                blaze.getNavigator().clearPathEntity();
                blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, 1.0D);
            }

            super.updateTask();
        }
    }
}
