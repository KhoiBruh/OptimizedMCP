package net.minecraft.entity.monster;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SkyBlock;
import net.minecraft.world.World;

public abstract class EntityMob extends EntityCreature implements IMob {
    public EntityMob(World worldIn) {
        super(worldIn);
        experienceValue = 5;
    }

    public void onLivingUpdate() {
        updateArmSwingProgress();
        float f = getBrightness(1.0F);

        if (f > 0.5F) {
            entityAge += 2;
        }

        super.onLivingUpdate();
    }

    public void onUpdate() {
        super.onUpdate();

        if (!worldObj.isRemote && worldObj.getDifficulty() == Difficulty.PEACEFUL) {
            setDead();
        }
    }

    protected String getSwimSound() {
        return "game.hostile.swim";
    }

    protected String getSplashSound() {
        return "game.hostile.swim.splash";
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else if (super.attackEntityFrom(source, amount)) {
            Entity entity = source.getEntity();
            return true;
        } else {
            return false;
        }
    }

    protected String getHurtSound() {
        return "game.hostile.hurt";
    }

    protected String getDeathSound() {
        return "game.hostile.die";
    }

    protected String getFallSoundString(int damageValue) {
        return damageValue > 4 ? "game.hostile.hurt.fall.big" : "game.hostile.hurt.fall.small";
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        float f = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        int i = 0;

        if (entityIn instanceof EntityLivingBase) {
            f += EnchantmentHelper.getModifierForCreature(getHeldItem(), ((EntityLivingBase) entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0) {
                entityIn.addVelocity(-MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F, 0.1D, MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F);
                motionX *= 0.6D;
                motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                entityIn.setFire(j * 4);
            }

            applyEnchantments(this, entityIn);
        }

        return flag;
    }

    public float getBlockPathWeight(BlockPos pos) {
        return 0.5F - worldObj.getLightBrightness(pos);
    }

    protected boolean isValidLightLevel() {
        BlockPos blockpos = new BlockPos(posX, getEntityBoundingBox().minY, posZ);

        if (worldObj.getLightFor(SkyBlock.SKY, blockpos) > rand.nextInt(32)) {
            return false;
        } else {
            int i = worldObj.getLightFromNeighbors(blockpos);

            if (worldObj.isThundering()) {
                int j = worldObj.getSkylightSubtracted();
                worldObj.setSkylightSubtracted(10);
                i = worldObj.getLightFromNeighbors(blockpos);
                worldObj.setSkylightSubtracted(j);
            }

            return i <= rand.nextInt(8);
        }
    }

    public boolean getCanSpawnHere() {
        return worldObj.getDifficulty() != Difficulty.PEACEFUL && isValidLightLevel() && super.getCanSpawnHere();
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
    }

    protected boolean canDropLoot() {
        return true;
    }
}
