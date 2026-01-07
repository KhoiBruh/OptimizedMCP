package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;

public class EntityEnderCrystal extends Entity {
    public int innerRotation;
    public int health;

    public EntityEnderCrystal(World worldIn) {
        super(worldIn);
        preventEntitySpawning = true;
        setSize(2.0F, 2.0F);
        health = 5;
        innerRotation = rand.nextInt(100000);
    }

    public EntityEnderCrystal(World worldIn, double x, double y, double z) {
        this(worldIn);
        setPosition(x, y, z);
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
        dataWatcher.addObject(8, health);
    }

    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        ++innerRotation;
        dataWatcher.updateObject(8, health);
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posY);
        int k = MathHelper.floor_double(posZ);

        if (worldObj.provider instanceof WorldProviderEnd && worldObj.getBlockState(new BlockPos(i, j, k)).getBlock() != Blocks.fire) {
            worldObj.setBlockState(new BlockPos(i, j, k), Blocks.fire.getDefaultState());
        }
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            if (!isDead && !worldObj.isRemote) {
                health = 0;

                if (health <= 0) {
                    setDead();

                    if (!worldObj.isRemote) {
                        worldObj.createExplosion(null, posX, posY, posZ, 6.0F, true);
                    }
                }
            }

            return true;
        }
    }
}
