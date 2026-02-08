package net.minecraft.entity.effect;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.List;

public class EntityLightningBolt extends EntityWeatherEffect {
    public long boltVertex;
    private int lightningState;
    private int boltLivingTime;

    public EntityLightningBolt(World worldIn, double posX, double posY, double posZ) {
        super(worldIn);
        setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
        lightningState = 2;
        boltVertex = rand.nextLong();
        boltLivingTime = rand.nextInt(3) + 1;
        BlockPos blockpos = new BlockPos(this);

        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doFireTick") && (worldIn.getDifficulty() == Difficulty.NORMAL || worldIn.getDifficulty() == Difficulty.HARD) && worldIn.isAreaLoaded(blockpos, 10)) {
            if (worldIn.getBlockState(blockpos).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldIn, blockpos)) {
                worldIn.setBlockState(blockpos, Blocks.fire.getDefaultState());
            }

            for (int i = 0; i < 4; ++i) {
                BlockPos blockpos1 = blockpos.add(rand.nextInt(3) - 1, rand.nextInt(3) - 1, rand.nextInt(3) - 1);

                if (worldIn.getBlockState(blockpos1).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldIn, blockpos1)) {
                    worldIn.setBlockState(blockpos1, Blocks.fire.getDefaultState());
                }
            }
        }
    }

    public void onUpdate() {
        super.onUpdate();

        if (lightningState == 2) {
            worldObj.playSoundEffect(posX, posY, posZ, "ambient.weather.thunder", 10000.0F, 0.8F + rand.nextFloat() * 0.2F);
            worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 2.0F, 0.5F + rand.nextFloat() * 0.2F);
        }

        --lightningState;

        if (lightningState < 0) {
            if (boltLivingTime == 0) {
                setDead();
            } else if (lightningState < -rand.nextInt(10)) {
                --boltLivingTime;
                lightningState = 1;
                boltVertex = rand.nextLong();
                BlockPos blockpos = new BlockPos(this);

                if (!worldObj.isRemote && worldObj.getGameRules().getBoolean("doFireTick") && worldObj.isAreaLoaded(blockpos, 10) && worldObj.getBlockState(blockpos).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldObj, blockpos)) {
                    worldObj.setBlockState(blockpos, Blocks.fire.getDefaultState());
                }
            }
        }

        if (lightningState >= 0) {
            if (worldObj.isRemote) {
                worldObj.setLastLightningBolt(2);
            } else {
                double d0 = 3.0D;
                List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX - d0, posY - d0, posZ - d0, posX + d0, posY + 6.0D + d0, posZ + d0));

                for (Entity entity : list) {
                    entity.onStruckByLightning(this);
                }
            }
        }
    }

    protected void entityInit() {
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
    }
}
