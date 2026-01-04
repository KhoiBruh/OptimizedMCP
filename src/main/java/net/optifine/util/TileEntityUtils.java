package net.optifine.util;

import net.minecraft.src.Config;
import net.minecraft.tileentity.*;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;

public class TileEntityUtils {
    public static String getTileEntityName(IBlockAccess blockAccess, BlockPos blockPos) {
        TileEntity tileentity = blockAccess.getTileEntity(blockPos);
        return getTileEntityName(tileentity);
    }

    public static String getTileEntityName(TileEntity te) {
        if (!(te instanceof IWorldNameable iworldnameable)) {
            return null;
        } else {
            updateTileEntityName(te);
            return !iworldnameable.hasCustomName() ? null : iworldnameable.getName();
        }
    }

    public static void updateTileEntityName(TileEntity te) {
        BlockPos blockpos = te.getPos();
        String s = getTileEntityRawName(te);

        if (s == null) {
            String s1 = getServerTileEntityRawName(blockpos);
            s1 = Config.normalize(s1);
            setTileEntityRawName(te, s1);
        }
    }

    public static String getServerTileEntityRawName(BlockPos blockPos) {
        TileEntity tileentity = IntegratedServerUtils.getTileEntity(blockPos);
        return tileentity == null ? null : getTileEntityRawName(tileentity);
    }

    public static String getTileEntityRawName(TileEntity te) {
        switch (te) {
            case TileEntityBeacon tileEntityBeacon -> {
                return tileEntityBeacon.customName;
            }
            case TileEntityBrewingStand tileEntityBrewingStand -> {
                return tileEntityBrewingStand.customName;
            }
            case TileEntityEnchantmentTable tileEntityEnchantmentTable -> {
                return tileEntityEnchantmentTable.customName;
            }
            case TileEntityFurnace tileEntityFurnace -> {
                return tileEntityFurnace.furnaceCustomName;
            }
            case null, default -> {
                if (te instanceof IWorldNameable iworldnameable) {

                    if (iworldnameable.hasCustomName()) {
                        return iworldnameable.getName();
                    }
                }

                return null;
            }
        }
    }

    public static boolean setTileEntityRawName(TileEntity te, String name) {
        switch (te) {
            case TileEntityBeacon tileEntityBeacon -> {
                tileEntityBeacon.customName = name;
                return true;
            }
            case TileEntityBrewingStand tileEntityBrewingStand -> {
                tileEntityBrewingStand.customName = name;
                return true;
            }
            case TileEntityEnchantmentTable tileEntityEnchantmentTable -> {
                tileEntityEnchantmentTable.customName = name;
                return true;
            }
            case TileEntityFurnace tileEntityFurnace -> {
                tileEntityFurnace.furnaceCustomName = name;
                return true;
            }
            case TileEntityChest tileEntityChest -> {
                tileEntityChest.setCustomName(name);
                return true;
            }
            case TileEntityDispenser tileEntityDispenser -> {
                tileEntityDispenser.setCustomName(name);
                return true;
            }
            case TileEntityHopper tileEntityHopper -> {
                tileEntityHopper.setCustomName(name);
                return true;
            }
            case null, default -> {
                return false;
            }
        }
    }
}
