package net.minecraft.entity.boss;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.List;

public class EntityWither extends EntityMob implements IBossDisplayData, IRangedAttackMob {
    private static final Predicate<Entity> attackEntitySelector = p_apply_1_ -> p_apply_1_ instanceof EntityLivingBase && ((EntityLivingBase) p_apply_1_).getCreatureAttribute() != CreatureAttribute.UNDEAD;
    private final float[] field_82220_d = new float[2];
    private final float[] field_82221_e = new float[2];
    private final float[] field_82217_f = new float[2];
    private final float[] field_82218_g = new float[2];
    private final int[] field_82223_h = new int[2];
    private final int[] field_82224_i = new int[2];
    private int blockBreakCounter;

    public EntityWither(World worldIn) {
        super(worldIn);
        setHealth(getMaxHealth());
        setSize(0.9F, 3.5F);
        isImmuneToFire = true;
        ((PathNavigateGround) getNavigator()).setCanSwim(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIArrowAttack(this, 1.0D, 40, 20.0F));
        tasks.addTask(5, new EntityAIWander(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 0, false, false, attackEntitySelector));
        experienceValue = 50;
    }

    public static boolean canDestroyBlock(Block p_181033_0_) {
        return p_181033_0_ != Blocks.bedrock && p_181033_0_ != Blocks.end_portal && p_181033_0_ != Blocks.end_portal_frame && p_181033_0_ != Blocks.command_block && p_181033_0_ != Blocks.barrier;
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(17, 0);
        dataWatcher.addObject(18, 0);
        dataWatcher.addObject(19, 0);
        dataWatcher.addObject(20, 0);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Invul", getInvulTime());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setInvulTime(tagCompund.getInteger("Invul"));
    }

    protected String getLivingSound() {
        return "mob.wither.idle";
    }

    protected String getHurtSound() {
        return "mob.wither.hurt";
    }

    protected String getDeathSound() {
        return "mob.wither.death";
    }

    public void onLivingUpdate() {
        motionY *= 0.6000000238418579D;

        if (!worldObj.isRemote && getWatchedTargetId(0) > 0) {
            Entity entity = worldObj.getEntityByID(getWatchedTargetId(0));

            if (entity != null) {
                if (posY < entity.posY || !isArmored() && posY < entity.posY + 5.0D) {
                    if (motionY < 0.0D) {
                        motionY = 0.0D;
                    }

                    motionY += (0.5D - motionY) * 0.6000000238418579D;
                }

                double d0 = entity.posX - posX;
                double d1 = entity.posZ - posZ;
                double d3 = d0 * d0 + d1 * d1;

                if (d3 > 9.0D) {
                    double d5 = MathHelper.sqrt_double(d3);
                    motionX += (d0 / d5 * 0.5D - motionX) * 0.6000000238418579D;
                    motionZ += (d1 / d5 * 0.5D - motionZ) * 0.6000000238418579D;
                }
            }
        }

        if (motionX * motionX + motionZ * motionZ > 0.05000000074505806D) {
            rotationYaw = (float) MathHelper.atan2(motionZ, motionX) * (180F / (float) Math.PI) - 90.0F;
        }

        super.onLivingUpdate();

        for (int i = 0; i < 2; ++i) {
            field_82218_g[i] = field_82221_e[i];
            field_82217_f[i] = field_82220_d[i];
        }

        for (int j = 0; j < 2; ++j) {
            int k = getWatchedTargetId(j + 1);
            Entity entity1 = null;

            if (k > 0) {
                entity1 = worldObj.getEntityByID(k);
            }

            if (entity1 != null) {
                double d11 = func_82214_u(j + 1);
                double d12 = func_82208_v(j + 1);
                double d13 = func_82213_w(j + 1);
                double d6 = entity1.posX - d11;
                double d7 = entity1.posY + (double) entity1.getEyeHeight() - d12;
                double d8 = entity1.posZ - d13;
                double d9 = MathHelper.sqrt_double(d6 * d6 + d8 * d8);
                float f = (float) (MathHelper.atan2(d8, d6) * 180.0D / Math.PI) - 90.0F;
                float f1 = (float) (-(MathHelper.atan2(d7, d9) * 180.0D / Math.PI));
                field_82220_d[j] = func_82204_b(field_82220_d[j], f1, 40.0F);
                field_82221_e[j] = func_82204_b(field_82221_e[j], f, 10.0F);
            } else {
                field_82221_e[j] = func_82204_b(field_82221_e[j], renderYawOffset, 10.0F);
            }
        }

        boolean flag = isArmored();

        for (int l = 0; l < 3; ++l) {
            double d10 = func_82214_u(l);
            double d2 = func_82208_v(l);
            double d4 = func_82213_w(l);
            worldObj.spawnParticle(ParticleTypes.SMOKE_NORMAL, d10 + rand.nextGaussian() * 0.30000001192092896D, d2 + rand.nextGaussian() * 0.30000001192092896D, d4 + rand.nextGaussian() * 0.30000001192092896D, 0.0D, 0.0D, 0.0D);

            if (flag && worldObj.rand.nextInt(4) == 0) {
                worldObj.spawnParticle(ParticleTypes.SPELL_MOB, d10 + rand.nextGaussian() * 0.30000001192092896D, d2 + rand.nextGaussian() * 0.30000001192092896D, d4 + rand.nextGaussian() * 0.30000001192092896D, 0.699999988079071D, 0.699999988079071D, 0.5D);
            }
        }

        if (getInvulTime() > 0) {
            for (int i1 = 0; i1 < 3; ++i1) {
                worldObj.spawnParticle(ParticleTypes.SPELL_MOB, posX + rand.nextGaussian(), posY + (double) (rand.nextFloat() * 3.3F), posZ + rand.nextGaussian(), 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D);
            }
        }
    }

    protected void updateAITasks() {
        if (getInvulTime() > 0) {
            int j1 = getInvulTime() - 1;

            if (j1 <= 0) {
                worldObj.newExplosion(this, posX, posY + (double) getEyeHeight(), posZ, 7.0F, false, worldObj.getGameRules().getBoolean("mobGriefing"));
                worldObj.playBroadcastSound(1013, new BlockPos(this), 0);
            }

            setInvulTime(j1);

            if (ticksExisted % 10 == 0) {
                heal(10.0F);
            }
        } else {
            super.updateAITasks();

            for (int i = 1; i < 3; ++i) {
                if (ticksExisted >= field_82223_h[i - 1]) {
                    field_82223_h[i - 1] = ticksExisted + 10 + rand.nextInt(10);

                    if (worldObj.getDifficulty() == Difficulty.NORMAL || worldObj.getDifficulty() == Difficulty.HARD) {
                        int j3 = i - 1;
                        int k3 = field_82224_i[i - 1];
                        field_82224_i[j3] = field_82224_i[i - 1] + 1;

                        if (k3 > 15) {
                            float f = 10.0F;
                            float f1 = 5.0F;
                            double d0 = MathHelper.getRandomDoubleInRange(rand, posX - (double) f, posX + (double) f);
                            double d1 = MathHelper.getRandomDoubleInRange(rand, posY - (double) f1, posY + (double) f1);
                            double d2 = MathHelper.getRandomDoubleInRange(rand, posZ - (double) f, posZ + (double) f);
                            launchWitherSkullToCoords(i + 1, d0, d1, d2, true);
                            field_82224_i[i - 1] = 0;
                        }
                    }

                    int k1 = getWatchedTargetId(i);

                    if (k1 > 0) {
                        Entity entity = worldObj.getEntityByID(k1);

                        if (entity != null && entity.isEntityAlive() && getDistanceSqToEntity(entity) <= 900.0D && canEntityBeSeen(entity)) {
                            if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage) {
                                updateWatchedTargetId(i, 0);
                            } else {
                                launchWitherSkullToEntity(i + 1, (EntityLivingBase) entity);
                                field_82223_h[i - 1] = ticksExisted + 40 + rand.nextInt(20);
                                field_82224_i[i - 1] = 0;
                            }
                        } else {
                            updateWatchedTargetId(i, 0);
                        }
                    } else {
                        List<EntityLivingBase> list = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().expand(20.0D, 8.0D, 20.0D), Predicates.and(attackEntitySelector, EntitySelectors.NOT_SPECTATING));

                        for (int j2 = 0; j2 < 10 && !list.isEmpty(); ++j2) {
                            EntityLivingBase entitylivingbase = list.get(rand.nextInt(list.size()));

                            if (entitylivingbase != this && entitylivingbase.isEntityAlive() && canEntityBeSeen(entitylivingbase)) {
                                if (entitylivingbase instanceof EntityPlayer) {
                                    if (!((EntityPlayer) entitylivingbase).capabilities.disableDamage) {
                                        updateWatchedTargetId(i, entitylivingbase.getEntityId());
                                    }
                                } else {
                                    updateWatchedTargetId(i, entitylivingbase.getEntityId());
                                }

                                break;
                            }

                            list.remove(entitylivingbase);
                        }
                    }
                }
            }

            if (getAttackTarget() != null) {
                updateWatchedTargetId(0, getAttackTarget().getEntityId());
            } else {
                updateWatchedTargetId(0, 0);
            }

            if (blockBreakCounter > 0) {
                --blockBreakCounter;

                if (blockBreakCounter == 0 && worldObj.getGameRules().getBoolean("mobGriefing")) {
                    int i1 = MathHelper.floor_double(posY);
                    int l1 = MathHelper.floor_double(posX);
                    int i2 = MathHelper.floor_double(posZ);
                    boolean flag = false;

                    for (int k2 = -1; k2 <= 1; ++k2) {
                        for (int l2 = -1; l2 <= 1; ++l2) {
                            for (int j = 0; j <= 3; ++j) {
                                int i3 = l1 + k2;
                                int k = i1 + j;
                                int l = i2 + l2;
                                BlockPos blockpos = new BlockPos(i3, k, l);
                                Block block = worldObj.getBlockState(blockpos).getBlock();

                                if (block.getMaterial() != Material.air && canDestroyBlock(block)) {
                                    flag = worldObj.destroyBlock(blockpos, true) || flag;
                                }
                            }
                        }
                    }

                    if (flag) {
                        worldObj.playAuxSFXAtEntity(null, 1012, new BlockPos(this), 0);
                    }
                }
            }

            if (ticksExisted % 20 == 0) {
                heal(1.0F);
            }
        }
    }

    public void func_82206_m() {
        setInvulTime(220);
        setHealth(getMaxHealth() / 3.0F);
    }

    public void setInWeb() {
    }

    public int getTotalArmorValue() {
        return 4;
    }

    private double func_82214_u(int p_82214_1_) {
        if (p_82214_1_ <= 0) {
            return posX;
        } else {
            float f = (renderYawOffset + (float) (180 * (p_82214_1_ - 1))) / 180.0F * (float) Math.PI;
            float f1 = MathHelper.cos(f);
            return posX + (double) f1 * 1.3D;
        }
    }

    private double func_82208_v(int p_82208_1_) {
        return p_82208_1_ <= 0 ? posY + 3.0D : posY + 2.2D;
    }

    private double func_82213_w(int p_82213_1_) {
        if (p_82213_1_ <= 0) {
            return posZ;
        } else {
            float f = (renderYawOffset + (float) (180 * (p_82213_1_ - 1))) / 180.0F * (float) Math.PI;
            float f1 = MathHelper.sin(f);
            return posZ + (double) f1 * 1.3D;
        }
    }

    private float func_82204_b(float p_82204_1_, float p_82204_2_, float p_82204_3_) {
        float f = MathHelper.wrapAngleTo180_float(p_82204_2_ - p_82204_1_);

        if (f > p_82204_3_) {
            f = p_82204_3_;
        }

        if (f < -p_82204_3_) {
            f = -p_82204_3_;
        }

        return p_82204_1_ + f;
    }

    private void launchWitherSkullToEntity(int p_82216_1_, EntityLivingBase p_82216_2_) {
        launchWitherSkullToCoords(p_82216_1_, p_82216_2_.posX, p_82216_2_.posY + (double) p_82216_2_.getEyeHeight() * 0.5D, p_82216_2_.posZ, p_82216_1_ == 0 && rand.nextFloat() < 0.001F);
    }

    private void launchWitherSkullToCoords(int p_82209_1_, double x, double y, double z, boolean invulnerable) {
        worldObj.playAuxSFXAtEntity(null, 1014, new BlockPos(this), 0);
        double d0 = func_82214_u(p_82209_1_);
        double d1 = func_82208_v(p_82209_1_);
        double d2 = func_82213_w(p_82209_1_);
        double d3 = x - d0;
        double d4 = y - d1;
        double d5 = z - d2;
        EntityWitherSkull entitywitherskull = new EntityWitherSkull(worldObj, this, d3, d4, d5);

        if (invulnerable) {
            entitywitherskull.setInvulnerable(true);
        }

        entitywitherskull.posY = d1;
        entitywitherskull.posX = d0;
        entitywitherskull.posZ = d2;
        worldObj.spawnEntityInWorld(entitywitherskull);
    }

    public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
        launchWitherSkullToEntity(0, target);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else if (source != DamageSource.drown && !(source.getEntity() instanceof EntityWither)) {
            if (getInvulTime() > 0 && source != DamageSource.outOfWorld) {
                return false;
            } else {
                if (isArmored()) {
                    Entity entity = source.getSourceOfDamage();

                    if (entity instanceof EntityArrow) {
                        return false;
                    }
                }

                Entity entity1 = source.getEntity();

                if (!(entity1 instanceof EntityPlayer) && entity1 instanceof EntityLivingBase && ((EntityLivingBase) entity1).getCreatureAttribute() == getCreatureAttribute()) {
                    return false;
                } else {
                    if (blockBreakCounter <= 0) {
                        blockBreakCounter = 20;
                    }

                    for (int i = 0; i < field_82224_i.length; ++i) {
                        field_82224_i[i] += 3;
                    }

                    return super.attackEntityFrom(source, amount);
                }
            }
        } else {
            return false;
        }
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        EntityItem entityitem = dropItem(Items.nether_star, 1);

        if (entityitem != null) {
            entityitem.setNoDespawn();
        }

        if (!worldObj.isRemote) {
            for (EntityPlayer entityplayer : worldObj.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().expand(50.0D, 100.0D, 50.0D))) {
                entityplayer.triggerAchievement(AchievementList.killWither);
            }
        }
    }

    protected void despawnEntity() {
        entityAge = 0;
    }

    public int getBrightnessForRender(float partialTicks) {
        return 15728880;
    }

    public void fall(float distance, float damageMultiplier) {
    }

    public void addPotionEffect(PotionEffect potioneffectIn) {
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(300.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6000000238418579D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
    }

    public float func_82207_a(int p_82207_1_) {
        return field_82221_e[p_82207_1_];
    }

    public float func_82210_r(int p_82210_1_) {
        return field_82220_d[p_82210_1_];
    }

    public int getInvulTime() {
        return dataWatcher.getWatchableObjectInt(20);
    }

    public void setInvulTime(int p_82215_1_) {
        dataWatcher.updateObject(20, p_82215_1_);
    }

    public int getWatchedTargetId(int p_82203_1_) {
        return dataWatcher.getWatchableObjectInt(17 + p_82203_1_);
    }

    public void updateWatchedTargetId(int targetOffset, int newId) {
        dataWatcher.updateObject(17 + targetOffset, newId);
    }

    public boolean isArmored() {
        return getHealth() <= getMaxHealth() / 2.0F;
    }

    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }

    public void mountEntity(Entity entityIn) {
        ridingEntity = null;
    }
}
