package net.minecraft.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;

import java.util.List;

public class EntityMinecartHopper extends EntityMinecartContainer implements IHopper {
    private boolean isBlocked = true;
    private int transferTicker = -1;
    private final BlockPos field_174900_c = BlockPos.ORIGIN;

    public EntityMinecartHopper(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartHopper(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public EntityMinecart.EnumMinecartType getMinecartType() {
        return EntityMinecart.EnumMinecartType.HOPPER;
    }

    public IBlockState getDefaultDisplayTile() {
        return Blocks.hopper.getDefaultState();
    }

    public int getDefaultDisplayTileOffset() {
        return 1;
    }

    public int getSizeInventory() {
        return 5;
    }

    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
        boolean flag = !receivingPower;

        if (flag != isBlocked) {
            isBlocked = flag;
        }
    }

    public boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean p_96110_1_) {
        isBlocked = p_96110_1_;
    }

    public World getWorld() {
        return worldObj;
    }

    public double getXPos() {
        return posX;
    }

    public double getYPos() {
        return posY + 0.5D;
    }

    public double getZPos() {
        return posZ;
    }

    public void onUpdate() {
        super.onUpdate();

        if (!worldObj.isRemote && isEntityAlive() && isBlocked) {
            BlockPos blockpos = new BlockPos(this);

            if (blockpos.equals(field_174900_c)) {
                --transferTicker;
            } else {
                transferTicker = 0;
            }

            if (!canTransfer()) {
                transferTicker = 0;

                if (func_96112_aD()) {
                    transferTicker = 4;
                    markDirty();
                }
            }
        }
    }

    public boolean func_96112_aD() {
        if (TileEntityHopper.captureDroppedItems(this)) {
            return true;
        } else {
            List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(0.25D, 0.0D, 0.25D), EntitySelectors.selectAnything);

            if (!list.isEmpty()) {
                TileEntityHopper.putDropInInventoryAllSlots(this, list.getFirst());
            }

            return false;
        }
    }

    public void killMinecart(DamageSource source) {
        super.killMinecart(source);

        if (worldObj.getGameRules().getBoolean("doEntityDrops")) {
            dropItemWithOffset(Item.getItemFromBlock(Blocks.hopper), 1, 0.0F);
        }
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("TransferCooldown", transferTicker);
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        transferTicker = tagCompund.getInteger("TransferCooldown");
    }

    public void setTransferTicker(int p_98042_1_) {
        transferTicker = p_98042_1_;
    }

    public boolean canTransfer() {
        return transferTicker > 0;
    }

    public String getGuiID() {
        return "minecraft:hopper";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerHopper(playerInventory, this, playerIn);
    }
}
