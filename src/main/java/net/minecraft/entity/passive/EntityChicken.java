package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityChicken extends EntityAnimal {
    public float wingRotation;
    public float destPos;
    public float field_70884_g;
    public float field_70888_h;
    public float wingRotDelta = 1.0F;
    public int timeUntilNextEgg;
    public boolean chickenJockey;

    public EntityChicken(World worldIn) {
        super(worldIn);
        setSize(0.4F, 0.7F);
        timeUntilNextEgg = rand.nextInt(6000) + 6000;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.wheat_seeds, false));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        tasks.addTask(5, new EntityAIWander(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
    }

    public float getEyeHeight() {
        return height;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
        field_70888_h = wingRotation;
        field_70884_g = destPos;
        destPos = (float) ((double) destPos + (double) (onGround ? -1 : 4) * 0.3D);
        destPos = MathHelper.clamp(destPos, 0.0F, 1.0F);

        if (!onGround && wingRotDelta < 1.0F) {
            wingRotDelta = 1.0F;
        }

        wingRotDelta = (float) ((double) wingRotDelta * 0.9D);

        if (!onGround && motionY < 0.0D) {
            motionY *= 0.6D;
        }

        wingRotation += wingRotDelta * 2.0F;

        if (!worldObj.isRemote && !isChild() && !chickenJockey && --timeUntilNextEgg <= 0) {
            playSound("mob.chicken.plop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            dropItem(Items.egg, 1);
            timeUntilNextEgg = rand.nextInt(6000) + 6000;
        }
    }

    public void fall(float distance, float damageMultiplier) {
    }

    protected String getLivingSound() {
        return "mob.chicken.say";
    }

    protected String getHurtSound() {
        return "mob.chicken.hurt";
    }

    protected String getDeathSound() {
        return "mob.chicken.hurt";
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.chicken.step", 0.15F, 1.0F);
    }

    protected Item getDropItem() {
        return Items.feather;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int i = rand.nextInt(3) + rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j) {
            dropItem(Items.feather, 1);
        }

        if (isBurning()) {
            dropItem(Items.cooked_chicken, 1);
        } else {
            dropItem(Items.chicken, 1);
        }
    }

    public EntityChicken createChild(EntityAgeable ageable) {
        return new EntityChicken(worldObj);
    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == Items.wheat_seeds;
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        chickenJockey = tagCompund.getBoolean("IsChickenJockey");

        if (tagCompund.hasKey("EggLayTime")) {
            timeUntilNextEgg = tagCompund.getInteger("EggLayTime");
        }
    }

    protected int getExperiencePoints(EntityPlayer player) {
        return chickenJockey ? 10 : super.getExperiencePoints(player);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("IsChickenJockey", chickenJockey);
        tagCompound.setInteger("EggLayTime", timeUntilNextEgg);
    }

    protected boolean canDespawn() {
        return chickenJockey && riddenByEntity == null;
    }

    public void updateRiderPosition() {
        super.updateRiderPosition();
        float f = MathHelper.sin(renderYawOffset * (float) Math.PI / 180.0F);
        float f1 = MathHelper.cos(renderYawOffset * (float) Math.PI / 180.0F);
        float f2 = 0.1F;
        float f3 = 0.0F;
        riddenByEntity.setPosition(posX + (double) (f2 * f), posY + (double) (height * 0.5F) + riddenByEntity.getYOffset() + (double) f3, posZ - (double) (f2 * f1));

        if (riddenByEntity instanceof EntityLivingBase) {
            ((EntityLivingBase) riddenByEntity).renderYawOffset = renderYawOffset;
        }
    }

    public boolean isChickenJockey() {
        return chickenJockey;
    }

    public void setChickenJockey(boolean jockey) {
        chickenJockey = jockey;
    }
}
