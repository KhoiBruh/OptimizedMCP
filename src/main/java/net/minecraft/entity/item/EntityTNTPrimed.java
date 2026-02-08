package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ParticleTypes;
import net.minecraft.world.World;

public class EntityTNTPrimed extends Entity {
    public int fuse;
    private EntityLivingBase tntPlacedBy;

    public EntityTNTPrimed(World worldIn) {
        super(worldIn);
        preventEntitySpawning = true;
        setSize(0.98F, 0.98F);
    }

    public EntityTNTPrimed(World worldIn, double x, double y, double z, EntityLivingBase igniter) {
        this(worldIn);
        setPosition(x, y, z);
        float f = (float) (Math.random() * Math.PI * 2.0D);
        motionX = -((float) Math.sin(f)) * 0.02F;
        motionY = 0.20000000298023224D;
        motionZ = -((float) Math.cos(f)) * 0.02F;
        fuse = 80;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        tntPlacedBy = igniter;
    }

    protected void entityInit() {
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean canBeCollidedWith() {
        return !isDead;
    }

    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY -= 0.03999999910593033D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;

        if (onGround) {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
            motionY *= -0.5D;
        }

        if (fuse-- <= 0) {
            setDead();

            if (!worldObj.isRemote) {
                explode();
            }
        } else {
            handleWaterMovement();
            worldObj.spawnParticle(ParticleTypes.SMOKE_NORMAL, posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    private void explode() {
        float f = 4.0F;
        worldObj.createExplosion(this, posX, posY + (double) (height / 16.0F), posZ, f, true);
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setByte("Fuse", (byte) fuse);
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        fuse = tagCompund.getByte("Fuse");
    }

    public EntityLivingBase getTntPlacedBy() {
        return tntPlacedBy;
    }

    public float getEyeHeight() {
        return 0.0F;
    }
}
