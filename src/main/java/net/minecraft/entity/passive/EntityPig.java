package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityPig extends EntityAnimal {
    private final EntityAIControlledByPlayer aiControlledByPlayer;

    public EntityPig(World worldIn) {
        super(worldIn);
        setSize(0.9F, 0.9F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        tasks.addTask(2, aiControlledByPlayer = new EntityAIControlledByPlayer(this, 0.3F));
        tasks.addTask(3, new EntityAIMate(this, 1.0D));
        tasks.addTask(4, new EntityAITempt(this, 1.2D, Items.carrot_on_a_stick, false));
        tasks.addTask(4, new EntityAITempt(this, 1.2D, Items.carrot, false));
        tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
        tasks.addTask(6, new EntityAIWander(this, 1.0D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    public boolean canBeSteered() {
        ItemStack itemstack = ((EntityPlayer) riddenByEntity).getHeldItem();
        return itemstack != null && itemstack.getItem() == Items.carrot_on_a_stick;
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Saddle", getSaddled());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setSaddled(tagCompund.getBoolean("Saddle"));
    }

    protected String getLivingSound() {
        return "mob.pig.say";
    }

    protected String getHurtSound() {
        return "mob.pig.say";
    }

    protected String getDeathSound() {
        return "mob.pig.death";
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.pig.step", 0.15F, 1.0F);
    }

    public boolean interact(EntityPlayer player) {
        if (super.interact(player)) {
            return true;
        } else if (!getSaddled() || worldObj.isRemote || riddenByEntity != null && riddenByEntity != player) {
            return false;
        } else {
            player.mountEntity(this);
            return true;
        }
    }

    protected Item getDropItem() {
        return isBurning() ? Items.cooked_porkchop : Items.porkchop;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int i = rand.nextInt(3) + 1 + rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j) {
            if (isBurning()) {
                dropItem(Items.cooked_porkchop, 1);
            } else {
                dropItem(Items.porkchop, 1);
            }
        }

        if (getSaddled()) {
            dropItem(Items.saddle, 1);
        }
    }

    public boolean getSaddled() {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setSaddled(boolean saddled) {
        if (saddled) {
            dataWatcher.updateObject(16, (byte) 1);
        } else {
            dataWatcher.updateObject(16, (byte) 0);
        }
    }

    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
        if (!worldObj.isRemote && !isDead) {
            EntityPigZombie entitypigzombie = new EntityPigZombie(worldObj);
            entitypigzombie.setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
            entitypigzombie.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            entitypigzombie.setNoAI(isAIDisabled());

            if (hasCustomName()) {
                entitypigzombie.setCustomNameTag(getCustomNameTag());
                entitypigzombie.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
            }

            worldObj.spawnEntityInWorld(entitypigzombie);
            setDead();
        }
    }

    public void fall(float distance, float damageMultiplier) {
        super.fall(distance, damageMultiplier);

        if (distance > 5.0F && riddenByEntity instanceof EntityPlayer) {
            ((EntityPlayer) riddenByEntity).triggerAchievement(AchievementList.flyPig);
        }
    }

    public EntityPig createChild(EntityAgeable ageable) {
        return new EntityPig(worldObj);
    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == Items.carrot;
    }

    public EntityAIControlledByPlayer getAIControlledByPlayer() {
        return aiControlledByPlayer;
    }
}
