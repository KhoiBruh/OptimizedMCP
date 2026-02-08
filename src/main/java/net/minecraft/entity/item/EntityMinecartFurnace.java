package net.minecraft.entity.item;

import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityMinecartFurnace extends EntityMinecart {
    public double pushX;
    public double pushZ;
    private int fuel;

    public EntityMinecartFurnace(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartFurnace(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public MinecartType getMinecartType() {
        return MinecartType.FURNACE;
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    public void onUpdate() {
        super.onUpdate();

        if (fuel > 0) {
            --fuel;
        }

        if (fuel <= 0) {
            pushX = pushZ = 0.0D;
        }

        setMinecartPowered(fuel > 0);

        if (isMinecartPowered() && rand.nextInt(4) == 0) {
            worldObj.spawnParticle(ParticleTypes.SMOKE_LARGE, posX, posY + 0.8D, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    protected double getMaximumSpeed() {
        return 0.2D;
    }

    public void killMinecart(DamageSource source) {
        super.killMinecart(source);

        if (!source.isExplosion() && worldObj.getGameRules().getBoolean("doEntityDrops")) {
            entityDropItem(new ItemStack(Blocks.furnace, 1), 0.0F);
        }
    }

    protected void func_180460_a(BlockPos p_180460_1_, IBlockState p_180460_2_) {
        super.func_180460_a(p_180460_1_, p_180460_2_);
        double d0 = pushX * pushX + pushZ * pushZ;

        if (d0 > 1.0E-4D && motionX * motionX + motionZ * motionZ > 0.001D) {
            d0 = MathHelper.sqrt_double(d0);
            pushX /= d0;
            pushZ /= d0;

            if (pushX * motionX + pushZ * motionZ < 0.0D) {
                pushX = 0.0D;
                pushZ = 0.0D;
            } else {
                double d1 = d0 / getMaximumSpeed();
                pushX *= d1;
                pushZ *= d1;
            }
        }
    }

    protected void applyDrag() {
        double d0 = pushX * pushX + pushZ * pushZ;

        if (d0 > 1.0E-4D) {
            d0 = MathHelper.sqrt_double(d0);
            pushX /= d0;
            pushZ /= d0;
            double d1 = 1.0D;
            motionX *= 0.800000011920929D;
            motionY *= 0.0D;
            motionZ *= 0.800000011920929D;
            motionX += pushX * d1;
            motionZ += pushZ * d1;
        } else {
            motionX *= 0.9800000190734863D;
            motionY *= 0.0D;
            motionZ *= 0.9800000190734863D;
        }

        super.applyDrag();
    }

    public boolean interactFirst(EntityPlayer playerIn) {
        ItemStack itemstack = playerIn.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.coal) {
            if (!playerIn.capabilities.isCreativeMode && --itemstack.stackSize == 0) {
                playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
            }

            fuel += 3600;
        }

        pushX = posX - playerIn.posX;
        pushZ = posZ - playerIn.posZ;
        return true;
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setDouble("PushX", pushX);
        tagCompound.setDouble("PushZ", pushZ);
        tagCompound.setShort("Fuel", (short) fuel);
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        pushX = tagCompund.getDouble("PushX");
        pushZ = tagCompund.getDouble("PushZ");
        fuel = tagCompund.getShort("Fuel");
    }

    protected boolean isMinecartPowered() {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    protected void setMinecartPowered(boolean p_94107_1_) {
        if (p_94107_1_) {
            dataWatcher.updateObject(16, (byte) (dataWatcher.getWatchableObjectByte(16) | 1));
        } else {
            dataWatcher.updateObject(16, (byte) (dataWatcher.getWatchableObjectByte(16) & -2));
        }
    }

    public IBlockState getDefaultDisplayTile() {
        return (isMinecartPowered() ? Blocks.lit_furnace : Blocks.furnace).getDefaultState().withProperty(BlockFurnace.FACING, Direction.NORTH);
    }
}
