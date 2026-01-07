package net.minecraft.world;

import net.minecraft.util.BlockPos;

public class ChunkCoordIntPair {
    public final int chunkXPos;
    public final int chunkZPos;
    private int cachedHashCode = 0;

    public ChunkCoordIntPair(int x, int z) {
        chunkXPos = x;
        chunkZPos = z;
    }

    public static long chunkXZ2Int(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

    public int hashCode() {
        if (cachedHashCode == 0) {
            int i = 1664525 * chunkXPos + 1013904223;
            int j = 1664525 * (chunkZPos ^ -559038737) + 1013904223;
            cachedHashCode = i ^ j;
        }

        return cachedHashCode;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChunkCoordIntPair chunkcoordintpair)) {
            return false;
        } else {
            return chunkXPos == chunkcoordintpair.chunkXPos && chunkZPos == chunkcoordintpair.chunkZPos;
        }
    }

    public int getCenterXPos() {
        return (chunkXPos << 4) + 8;
    }

    public int getCenterZPosition() {
        return (chunkZPos << 4) + 8;
    }

    public int getXStart() {
        return chunkXPos << 4;
    }

    public int getZStart() {
        return chunkZPos << 4;
    }

    public int getXEnd() {
        return (chunkXPos << 4) + 15;
    }

    public int getZEnd() {
        return (chunkZPos << 4) + 15;
    }

    public BlockPos getBlock(int x, int y, int z) {
        return new BlockPos((chunkXPos << 4) + x, y, (chunkZPos << 4) + z);
    }

    public BlockPos getCenterBlock(int y) {
        return new BlockPos(getCenterXPos(), y, getCenterZPosition());
    }

    public String toString() {
        return "[" + chunkXPos + ", " + chunkZPos + "]";
    }
}
