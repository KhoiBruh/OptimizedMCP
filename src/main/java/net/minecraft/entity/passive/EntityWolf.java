package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityWolf extends EntityTameable {
    private float headRotationCourse;
    private float headRotationCourseOld;
    private boolean isWet;
    private boolean isShaking;
    private float timeWolfIsShaking;
    private float prevTimeWolfIsShaking;

    public EntityWolf(World worldIn) {
        super(worldIn);
        setSize(0.6F, 0.8F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, aiSit);
        tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
        tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, true));
        tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        tasks.addTask(6, new EntityAIMate(this, 1.0D));
        tasks.addTask(7, new EntityAIWander(this, 1.0D));
        tasks.addTask(8, new EntityAIBeg(this, 8.0F));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(9, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(4, new EntityAITargetNonTamed(this, EntityAnimal.class, false, (Predicate<Entity>) p_apply_1_ -> p_apply_1_ instanceof EntitySheep || p_apply_1_ instanceof EntityRabbit));
        targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, EntitySkeleton.class, false));
        setTamed(false);
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);

        if (isTamed()) {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        } else {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        }

        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
    }

    public void setAttackTarget(EntityLivingBase entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);

        if (entitylivingbaseIn == null) {
            setAngry(false);
        } else if (!isTamed()) {
            setAngry(true);
        }
    }

    protected void updateAITasks() {
        dataWatcher.updateObject(18, getHealth());
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(18, getHealth());
        dataWatcher.addObject(19, (byte) 0);
        dataWatcher.addObject(20, (byte) EnumDyeColor.RED.getMetadata());
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.wolf.step", 0.15F, 1.0F);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Angry", isAngry());
        tagCompound.setByte("CollarColor", (byte) getCollarColor().getDyeDamage());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setAngry(tagCompund.getBoolean("Angry"));

        if (tagCompund.hasKey("CollarColor", 99)) {
            setCollarColor(EnumDyeColor.byDyeDamage(tagCompund.getByte("CollarColor")));
        }
    }

    protected String getLivingSound() {
        return isAngry() ? "mob.wolf.growl" : (rand.nextInt(3) == 0 ? (isTamed() && dataWatcher.getWatchableObjectFloat(18) < 10.0F ? "mob.wolf.whine" : "mob.wolf.panting") : "mob.wolf.bark");
    }

    protected String getHurtSound() {
        return "mob.wolf.hurt";
    }

    protected String getDeathSound() {
        return "mob.wolf.death";
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    protected Item getDropItem() {
        return Item.getItemById(-1);
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!worldObj.isRemote && isWet && !isShaking && !hasPath() && onGround) {
            isShaking = true;
            timeWolfIsShaking = 0.0F;
            prevTimeWolfIsShaking = 0.0F;
            worldObj.setEntityState(this, (byte) 8);
        }

        if (!worldObj.isRemote && getAttackTarget() == null && isAngry()) {
            setAngry(false);
        }
    }

    public void onUpdate() {
        super.onUpdate();
        headRotationCourseOld = headRotationCourse;

        if (isBegging()) {
            headRotationCourse += (1.0F - headRotationCourse) * 0.4F;
        } else {
            headRotationCourse += (0.0F - headRotationCourse) * 0.4F;
        }

        if (isWet()) {
            isWet = true;
            isShaking = false;
            timeWolfIsShaking = 0.0F;
            prevTimeWolfIsShaking = 0.0F;
        } else if ((isWet || isShaking) && isShaking) {
            if (timeWolfIsShaking == 0.0F) {
                playSound("mob.wolf.shake", getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }

            prevTimeWolfIsShaking = timeWolfIsShaking;
            timeWolfIsShaking += 0.05F;

            if (prevTimeWolfIsShaking >= 2.0F) {
                isWet = false;
                isShaking = false;
                prevTimeWolfIsShaking = 0.0F;
                timeWolfIsShaking = 0.0F;
            }

            if (timeWolfIsShaking > 0.4F) {
                float f = (float) getEntityBoundingBox().minY;
                int i = (int) (MathHelper.sin((timeWolfIsShaking - 0.4F) * (float) Math.PI) * 7.0F);

                for (int j = 0; j < i; ++j) {
                    float f1 = (rand.nextFloat() * 2.0F - 1.0F) * width * 0.5F;
                    float f2 = (rand.nextFloat() * 2.0F - 1.0F) * width * 0.5F;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX + (double) f1, f + 0.8F, posZ + (double) f2, motionX, motionY, motionZ);
                }
            }
        }
    }

    public boolean isWolfWet() {
        return isWet;
    }

    public float getShadingWhileWet(float p_70915_1_) {
        return 0.75F + (prevTimeWolfIsShaking + (timeWolfIsShaking - prevTimeWolfIsShaking) * p_70915_1_) / 2.0F * 0.25F;
    }

    public float getShakeAngle(float p_70923_1_, float p_70923_2_) {
        float f = (prevTimeWolfIsShaking + (timeWolfIsShaking - prevTimeWolfIsShaking) * p_70923_1_ + p_70923_2_) / 1.8F;

        if (f < 0.0F) {
            f = 0.0F;
        } else if (f > 1.0F) {
            f = 1.0F;
        }

        return MathHelper.sin(f * (float) Math.PI) * MathHelper.sin(f * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
    }

    public float getInterestedAngle(float p_70917_1_) {
        return (headRotationCourseOld + (headRotationCourse - headRotationCourseOld) * p_70917_1_) * 0.15F * (float) Math.PI;
    }

    public float getEyeHeight() {
        return height * 0.8F;
    }

    public int getVerticalFaceSpeed() {
        return isSitting() ? 20 : super.getVerticalFaceSpeed();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getEntity();
            aiSit.setSitting(false);

            if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) ((int) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue()));

        if (flag) {
            applyEnchantments(this, entityIn);
        }

        return flag;
    }

    public void setTamed(boolean tamed) {
        super.setTamed(tamed);

        if (tamed) {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        } else {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        }

        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);
    }

    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (isTamed()) {
            if (itemstack != null) {
                if (itemstack.getItem() instanceof ItemFood itemfood) {

                    if (itemfood.isWolfsFavoriteMeat() && dataWatcher.getWatchableObjectFloat(18) < 20.0F) {
                        if (!player.capabilities.isCreativeMode) {
                            --itemstack.stackSize;
                        }

                        heal((float) itemfood.getHealAmount(itemstack));

                        if (itemstack.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }

                        return true;
                    }
                } else if (itemstack.getItem() == Items.dye) {
                    EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(itemstack.getMetadata());

                    if (enumdyecolor != getCollarColor()) {
                        setCollarColor(enumdyecolor);

                        if (!player.capabilities.isCreativeMode && --itemstack.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }

                        return true;
                    }
                }
            }

            if (isOwner(player) && !worldObj.isRemote && !isBreedingItem(itemstack)) {
                aiSit.setSitting(!isSitting());
                isJumping = false;
                navigator.clearPathEntity();
                setAttackTarget(null);
            }
        } else if (itemstack != null && itemstack.getItem() == Items.bone && !isAngry()) {
            if (!player.capabilities.isCreativeMode) {
                --itemstack.stackSize;
            }

            if (itemstack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            if (!worldObj.isRemote) {
                if (rand.nextInt(3) == 0) {
                    setTamed(true);
                    navigator.clearPathEntity();
                    setAttackTarget(null);
                    aiSit.setSitting(true);
                    setHealth(20.0F);
                    setOwnerId(player.getUniqueID().toString());
                    playTameEffect(true);
                    worldObj.setEntityState(this, (byte) 7);
                } else {
                    playTameEffect(false);
                    worldObj.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }

        return super.interact(player);
    }

    public void handleStatusUpdate(byte id) {
        if (id == 8) {
            isShaking = true;
            timeWolfIsShaking = 0.0F;
            prevTimeWolfIsShaking = 0.0F;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public float getTailRotation() {
        return isAngry() ? 1.5393804F : (isTamed() ? (0.55F - (20.0F - dataWatcher.getWatchableObjectFloat(18)) * 0.02F) * (float) Math.PI : ((float) Math.PI / 5F));
    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && (stack.getItem() instanceof ItemFood && ((ItemFood) stack.getItem()).isWolfsFavoriteMeat());
    }

    public int getMaxSpawnedInChunk() {
        return 8;
    }

    public boolean isAngry() {
        return (dataWatcher.getWatchableObjectByte(16) & 2) != 0;
    }

    public void setAngry(boolean angry) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (angry) {
            dataWatcher.updateObject(16, (byte) (b0 | 2));
        } else {
            dataWatcher.updateObject(16, (byte) (b0 & -3));
        }
    }

    public EnumDyeColor getCollarColor() {
        return EnumDyeColor.byDyeDamage(dataWatcher.getWatchableObjectByte(20) & 15);
    }

    public void setCollarColor(EnumDyeColor collarcolor) {
        dataWatcher.updateObject(20, (byte) (collarcolor.getDyeDamage() & 15));
    }

    public EntityWolf createChild(EntityAgeable ageable) {
        EntityWolf entitywolf = new EntityWolf(worldObj);
        String s = getOwnerId();

        if (s != null && !s.trim().isEmpty()) {
            entitywolf.setOwnerId(s);
            entitywolf.setTamed(true);
        }

        return entitywolf;
    }

    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        } else if (!isTamed()) {
            return false;
        } else if (!(otherAnimal instanceof EntityWolf entitywolf)) {
            return false;
        } else {
            return entitywolf.isTamed() && (!entitywolf.isSitting() && isInLove() && entitywolf.isInLove());
        }
    }

    public boolean isBegging() {
        return dataWatcher.getWatchableObjectByte(19) == 1;
    }

    public void setBegging(boolean beg) {
        if (beg) {
            dataWatcher.updateObject(19, (byte) 1);
        } else {
            dataWatcher.updateObject(19, (byte) 0);
        }
    }

    protected boolean canDespawn() {
        return !isTamed() && ticksExisted > 2400;
    }

    public boolean shouldAttackEntity(EntityLivingBase p_142018_1_, EntityLivingBase p_142018_2_) {
        if (!(p_142018_1_ instanceof EntityCreeper) && !(p_142018_1_ instanceof EntityGhast)) {
            if (p_142018_1_ instanceof EntityWolf entitywolf) {

                if (entitywolf.isTamed() && entitywolf.getOwner() == p_142018_2_) {
                    return false;
                }
            }

            return (!(p_142018_1_ instanceof EntityPlayer) || !(p_142018_2_ instanceof EntityPlayer) || ((EntityPlayer) p_142018_2_).canAttackPlayer((EntityPlayer) p_142018_1_)) && (!(p_142018_1_ instanceof EntityHorse) || !((EntityHorse) p_142018_1_).isTame());
        } else {
            return false;
        }
    }

    public boolean allowLeashing() {
        return !isAngry() && super.allowLeashing();
    }
}
