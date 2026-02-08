package net.minecraft.entity.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityMinecartEmpty extends EntityMinecart {
    public EntityMinecartEmpty(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartEmpty(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public boolean interactFirst(EntityPlayer playerIn) {
        if (riddenByEntity != null && riddenByEntity instanceof EntityPlayer && riddenByEntity != playerIn) {
            return true;
        } else if (riddenByEntity != null && riddenByEntity != playerIn) {
            return false;
        } else {
            if (!worldObj.isRemote) {
                playerIn.mountEntity(this);
            }

            return true;
        }
    }

    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
        if (receivingPower) {
            if (riddenByEntity != null) {
                riddenByEntity.mountEntity(null);
            }

            if (getRollingAmplitude() == 0) {
                setRollingDirection(-getRollingDirection());
                setRollingAmplitude(10);
                setDamage(50.0F);
                setBeenAttacked();
            }
        }
    }

    public MinecartType getMinecartType() {
        return MinecartType.RIDEABLE;
    }
}
