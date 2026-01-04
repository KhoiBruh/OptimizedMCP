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
        return switch (te) {
            case TileEntityBeacon tileEntityBeacon -> tileEntityBeacon.customName;
            case TileEntityBrewingStand tileEntityBrewingStand -> tileEntityBrewingStand.customName;
            case TileEntityEnchantmentTable tileEntityEnchantmentTable -> tileEntityEnchantmentTable.customName;
            case TileEntityFurnace tileEntityFurnace -> tileEntityFurnace.furnaceCustomName;
            case null, default -> {
                if (te instanceof IWorldNameable iworldnameable) {

                    if (iworldnameable.hasCustomName()) {
                        yield iworldnameable.getName();
                    }
                }

                yield null;
            }
        };
    }

    public static boolean setTileEntityRawName(TileEntity te, String name) {
        return switch (te) {
            case TileEntityBeacon tileEntityBeacon -> {
                tileEntityBeacon.customName = name;
                yield true;
            }
            case TileEntityBrewingStand tileEntityBrewingStand -> {
                tileEntityBrewingStand.customName = name;
                yield true;
            }
            case TileEntityEnchantmentTable tileEntityEnchantmentTable -> {
                tileEntityEnchantmentTable.customName = name;
                yield true;
            }
            case TileEntityFurnace tileEntityFurnace -> {
                tileEntityFurnace.furnaceCustomName = name;
                yield true;
            }
            case TileEntityChest tileEntityChest -> {
                tileEntityChest.setCustomName(name);
                yield true;
            }
            case TileEntityDispenser tileEntityDispenser -> {
                tileEntityDispenser.setCustomName(name);
                yield true;
            }
            case TileEntityHopper tileEntityHopper -> {
                tileEntityHopper.setCustomName(name);
                yield true;
            }
            case null, default -> false;
        };
    }
}
