package net.optifine;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public enum BlockDir {
    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST),
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
    DOWN_NORTH(EnumFacing.DOWN, EnumFacing.NORTH),
    DOWN_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH),
    UP_NORTH(EnumFacing.UP, EnumFacing.NORTH),
    UP_SOUTH(EnumFacing.UP, EnumFacing.SOUTH),
    DOWN_WEST(EnumFacing.DOWN, EnumFacing.WEST),
    DOWN_EAST(EnumFacing.DOWN, EnumFacing.EAST),
    UP_WEST(EnumFacing.UP, EnumFacing.WEST),
    UP_EAST(EnumFacing.UP, EnumFacing.EAST);

    private final EnumFacing facing1;
    private EnumFacing facing2;

    BlockDir(EnumFacing facing1) {
        this.facing1 = facing1;
    }

    BlockDir(EnumFacing facing1, EnumFacing facing2) {
        this.facing1 = facing1;
        this.facing2 = facing2;
    }

    BlockPos offset(BlockPos pos) {
        pos = pos.offset(facing1, 1);

        if (facing2 != null) {
            pos = pos.offset(facing2, 1);
        }

        return pos;
    }

}
