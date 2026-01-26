package net.minecraft.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.Random;

public class EntitySpider extends EntityMob {
    public EntitySpider(World worldIn) {
        super(worldIn);
        setSize(1.4F, 0.9F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
        tasks.addTask(4, new EntitySpider.AISpiderAttack(this, EntityPlayer.class));
        tasks.addTask(4, new EntitySpider.AISpiderAttack(this, EntityIronGolem.class));
        tasks.addTask(5, new EntityAIWander(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntitySpider.AISpiderTarget<>(this, EntityPlayer.class));
        targetTasks.addTask(3, new EntitySpider.AISpiderTarget<>(this, EntityIronGolem.class));
    }

    public double getMountedYOffset() {
        return height * 0.5F;
    }

    protected PathNavigate getNewNavigator(World worldIn) {
        return new PathNavigateClimber(this, worldIn);
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    public void onUpdate() {
        super.onUpdate();

        if (!worldObj.isRemote) {
            setBesideClimbableBlock(isCollidedHorizontally);
        }
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
    }

    protected String getLivingSound() {
        return "mob.spider.say";
    }

    protected String getHurtSound() {
        return "mob.spider.say";
    }

    protected String getDeathSound() {
        return "mob.spider.death";
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.spider.step", 0.15F, 1.0F);
    }

    protected Item getDropItem() {
        return Items.string;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        super.dropFewItems(wasRecentlyHit, lootingModifier);

        if (wasRecentlyHit && (rand.nextInt(3) == 0 || rand.nextInt(1 + lootingModifier) > 0)) {
            dropItem(Items.spider_eye, 1);
        }
    }

    public boolean isOnLadder() {
        return isBesideClimbableBlock();
    }

    public void setInWeb() {
    }

    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        return potioneffectIn.getPotionID() != Potion.poison.id && super.isPotionApplicable(potioneffectIn);
    }

    public boolean isBesideClimbableBlock() {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setBesideClimbableBlock(boolean p_70839_1_) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (p_70839_1_) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 = (byte) (b0 & -2);
        }

        dataWatcher.updateObject(16, b0);
    }

    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        if (worldObj.rand.nextInt(100) == 0) {
            EntitySkeleton entityskeleton = new EntitySkeleton(worldObj);
            entityskeleton.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
            entityskeleton.onInitialSpawn(difficulty, null);
            worldObj.spawnEntityInWorld(entityskeleton);
            entityskeleton.mountEntity(this);
        }

        if (livingdata == null) {
            livingdata = new EntitySpider.GroupData();

            if (worldObj.getDifficulty() == EnumDifficulty.HARD && worldObj.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
                ((EntitySpider.GroupData) livingdata).func_111104_a(worldObj.rand);
            }
        }

        if (livingdata instanceof EntitySpider.GroupData) {
            int i = ((EntitySpider.GroupData) livingdata).potionEffectId;

            if (i > 0 && Potion.potionTypes[i] != null) {
                addPotionEffect(new PotionEffect(i, Integer.MAX_VALUE));
            }
        }

        return livingdata;
    }

    public float getEyeHeight() {
        return 0.65F;
    }

    static class AISpiderAttack extends EntityAIAttackOnCollide {
        public AISpiderAttack(EntitySpider spider, Class<? extends Entity> targetClass) {
            super(spider, targetClass, 1.0D, true);
        }

        public boolean continueExecuting() {
            float f = attacker.getBrightness(1.0F);

            if (f >= 0.5F && attacker.getRNG().nextInt(100) == 0) {
                attacker.setAttackTarget(null);
                return false;
            } else {
                return super.continueExecuting();
            }
        }

        protected double func_179512_a(EntityLivingBase attackTarget) {
            return 4.0F + attackTarget.width;
        }
    }

    static class AISpiderTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget {
        public AISpiderTarget(EntitySpider spider, Class<T> classTarget) {
            super(spider, classTarget, true);
        }

        public boolean shouldExecute() {
            float f = taskOwner.getBrightness(1.0F);
            return !(f >= 0.5F) && super.shouldExecute();
        }
    }

    public static class GroupData implements IEntityLivingData {
        public int potionEffectId;

        public void func_111104_a(Random rand) {
            int i = rand.nextInt(5);

            if (i <= 1) {
                potionEffectId = Potion.moveSpeed.id;
            } else if (i == 2) {
                potionEffectId = Potion.damageBoost.id;
            } else if (i == 3) {
                potionEffectId = Potion.regeneration.id;
            } else {
                potionEffectId = Potion.invisibility.id;
            }
        }
    }
}
