package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityWitherSkull extends EntityFireball {
    public EntityWitherSkull(World worldIn) {
        super(worldIn);
        setSize(0.3125F, 0.3125F);
    }

    public EntityWitherSkull(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
        super(worldIn, shooter, accelX, accelY, accelZ);
        setSize(0.3125F, 0.3125F);
    }

    public EntityWitherSkull(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
        super(worldIn, x, y, z, accelX, accelY, accelZ);
        setSize(0.3125F, 0.3125F);
    }

    protected float getMotionFactor() {
        return isInvulnerable() ? 0.73F : super.getMotionFactor();
    }

    public boolean isBurning() {
        return false;
    }

    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
        float f = super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
        Block block = blockStateIn.getBlock();

        if (isInvulnerable() && EntityWither.canDestroyBlock(block)) {
            f = Math.min(0.8F, f);
        }

        return f;
    }

    protected void onImpact(MovingObjectPosition movingObject) {
        if (!worldObj.isRemote) {
            if (movingObject.entityHit != null) {
                if (shootingEntity != null) {
                    if (movingObject.entityHit.attackEntityFrom(DamageSource.causeMobDamage(shootingEntity), 8.0F)) {
                        if (!movingObject.entityHit.isEntityAlive()) {
                            shootingEntity.heal(5.0F);
                        } else {
                            applyEnchantments(shootingEntity, movingObject.entityHit);
                        }
                    }
                } else {
                    movingObject.entityHit.attackEntityFrom(DamageSource.magic, 5.0F);
                }

                if (movingObject.entityHit instanceof EntityLivingBase) {
                    int i = 0;

                    if (worldObj.getDifficulty() == EnumDifficulty.NORMAL) {
                        i = 10;
                    } else if (worldObj.getDifficulty() == EnumDifficulty.HARD) {
                        i = 40;
                    }

                    if (i > 0) {
                        ((EntityLivingBase) movingObject.entityHit).addPotionEffect(new PotionEffect(Potion.wither.id, 20 * i, 1));
                    }
                }
            }

            worldObj.newExplosion(this, posX, posY, posZ, 1.0F, false, worldObj.getGameRules().getBoolean("mobGriefing"));
            setDead();
        }
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    protected void entityInit() {
        dataWatcher.addObject(10, (byte) 0);
    }

    public boolean isInvulnerable() {
        return dataWatcher.getWatchableObjectByte(10) == 1;
    }

    public void setInvulnerable(boolean invulnerable) {
        dataWatcher.updateObject(10, (byte) (invulnerable ? 1 : 0));
    }
}
