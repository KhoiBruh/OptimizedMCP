package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityLargeFireball extends EntityFireball {
    public int explosionPower = 1;

    public EntityLargeFireball(World worldIn) {
        super(worldIn);
    }

    public EntityLargeFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
        super(worldIn, x, y, z, accelX, accelY, accelZ);
    }

    public EntityLargeFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
        super(worldIn, shooter, accelX, accelY, accelZ);
    }

    protected void onImpact(MovingObjectPosition movingObject) {
        if (!worldObj.isRemote) {
            if (movingObject.entityHit != null) {
                movingObject.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), 6.0F);
                applyEnchantments(shootingEntity, movingObject.entityHit);
            }

            boolean flag = worldObj.getGameRules().getBoolean("mobGriefing");
            worldObj.newExplosion(null, posX, posY, posZ, (float) explosionPower, flag, flag);
            setDead();
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("ExplosionPower", explosionPower);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("ExplosionPower", 99)) {
            explosionPower = tagCompund.getInteger("ExplosionPower");
        }
    }
}
