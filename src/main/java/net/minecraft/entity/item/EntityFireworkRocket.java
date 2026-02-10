package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFireworkRocket extends Entity {
    private int fireworkAge;
    private int lifetime;

    public EntityFireworkRocket(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    public EntityFireworkRocket(World worldIn, double x, double y, double z, ItemStack givenItem) {
        super(worldIn);
        fireworkAge = 0;
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
        int i = 1;

        if (givenItem != null && givenItem.hasTagCompound()) {
            dataWatcher.updateObject(8, givenItem);
            NBTTagCompound nbttagcompound = givenItem.getTagCompound();
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Fireworks");

            if (nbttagcompound1 != null) {
                i += nbttagcompound1.getByte("Flight");
            }
        }

        motionX = rand.nextGaussian() * 0.001D;
        motionZ = rand.nextGaussian() * 0.001D;
        motionY = 0.05D;
        lifetime = 10 * i + rand.nextInt(6) + rand.nextInt(7);
    }

    protected void entityInit() {
        dataWatcher.addObjectByDataType(8, 5);
    }

    public boolean isInRangeToRenderDist(double distance) {
        return distance < 4096.0D;
    }

    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, f) * 180.0D / Math.PI);
        }
    }

    public void onUpdate() {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        super.onUpdate();
        motionX *= 1.15D;
        motionZ *= 1.15D;
        motionY += 0.04D;
        moveEntity(motionX, motionY, motionZ);
        float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * 180.0D / Math.PI);

        for (rotationPitch = (float) (MathHelper.atan2(motionY, f) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F) {
        }

        while (rotationPitch - prevRotationPitch >= 180.0F) {
            prevRotationPitch += 360.0F;
        }

        while (rotationYaw - prevRotationYaw < -180.0F) {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F) {
            prevRotationYaw += 360.0F;
        }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

        if (fireworkAge == 0 && !isSilent()) {
            worldObj.playSoundAtEntity(this, "fireworks.launch", 3.0F, 1.0F);
        }

        ++fireworkAge;

        if (worldObj.isRemote) {
            worldObj.spawnParticle(ParticleTypes.FIREWORKS_SPARK, posX, posY - 0.3D, posZ, rand.nextGaussian() * 0.05D, -motionY * 0.5D, rand.nextGaussian() * 0.05D);
        }

        if (!worldObj.isRemote && fireworkAge > lifetime) {
            worldObj.setEntityState(this, (byte) 17);
            setDead();
        }
    }

    public void handleStatusUpdate(byte id) {
        if (id == 17 && worldObj.isRemote) {
            ItemStack itemstack = dataWatcher.getWatchableObjectItemStack(8);
            NBTTagCompound nbttagcompound = null;

            if (itemstack != null && itemstack.hasTagCompound()) {
                nbttagcompound = itemstack.getTagCompound().getCompoundTag("Fireworks");
            }

            worldObj.makeFireworks(posX, posY, posZ, motionX, motionY, motionZ, nbttagcompound);
        }

        super.handleStatusUpdate(id);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("Life", fireworkAge);
        tagCompound.setInteger("LifeTime", lifetime);
        ItemStack itemstack = dataWatcher.getWatchableObjectItemStack(8);

        if (itemstack != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            itemstack.writeToNBT(nbttagcompound);
            tagCompound.setTag("FireworksItem", nbttagcompound);
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        fireworkAge = tagCompund.getInteger("Life");
        lifetime = tagCompund.getInteger("LifeTime");
        NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("FireworksItem");

        if (nbttagcompound != null) {
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null) {
                dataWatcher.updateObject(8, itemstack);
            }
        }
    }

    public float getBrightness(float partialTicks) {
        return super.getBrightness(partialTicks);
    }

    public int getBrightnessForRender(float partialTicks) {
        return super.getBrightnessForRender(partialTicks);
    }

    public boolean canAttackWithItem() {
        return false;
    }
}
