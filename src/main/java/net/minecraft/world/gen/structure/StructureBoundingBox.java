package net.minecraft.world.gen.structure;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;

public class StructureBoundingBox {
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

    public StructureBoundingBox() {
    }

    public StructureBoundingBox(int[] coords) {
        if (coords.length == 6) {
            minX = coords[0];
            minY = coords[1];
            minZ = coords[2];
            maxX = coords[3];
            maxY = coords[4];
            maxZ = coords[5];
        }
    }

    public StructureBoundingBox(StructureBoundingBox structurebb) {
        minX = structurebb.minX;
        minY = structurebb.minY;
        minZ = structurebb.minZ;
        maxX = structurebb.maxX;
        maxY = structurebb.maxY;
        maxZ = structurebb.maxZ;
    }

    public StructureBoundingBox(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        minX = xMin;
        minY = yMin;
        minZ = zMin;
        maxX = xMax;
        maxY = yMax;
        maxZ = zMax;
    }

    public StructureBoundingBox(Vec3i vec1, Vec3i vec2) {
        minX = Math.min(vec1.getX(), vec2.getX());
        minY = Math.min(vec1.getY(), vec2.getY());
        minZ = Math.min(vec1.getZ(), vec2.getZ());
        maxX = Math.max(vec1.getX(), vec2.getX());
        maxY = Math.max(vec1.getY(), vec2.getY());
        maxZ = Math.max(vec1.getZ(), vec2.getZ());
    }

    public StructureBoundingBox(int xMin, int zMin, int xMax, int zMax) {
        minX = xMin;
        minZ = zMin;
        maxX = xMax;
        maxZ = zMax;
        minY = 1;
        maxY = 512;
    }

    public static StructureBoundingBox getNewBoundingBox() {
        return new StructureBoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static StructureBoundingBox getComponentToAddBoundingBox(int p_175897_0_, int p_175897_1_, int p_175897_2_, int p_175897_3_, int p_175897_4_, int p_175897_5_, int p_175897_6_, int p_175897_7_, int p_175897_8_, EnumFacing p_175897_9_) {
        return switch (p_175897_9_) {
            case NORTH ->
                    new StructureBoundingBox(p_175897_0_ + p_175897_3_, p_175897_1_ + p_175897_4_, p_175897_2_ - p_175897_8_ + 1 + p_175897_5_, p_175897_0_ + p_175897_6_ - 1 + p_175897_3_, p_175897_1_ + p_175897_7_ - 1 + p_175897_4_, p_175897_2_ + p_175897_5_);
            case SOUTH ->
                    new StructureBoundingBox(p_175897_0_ + p_175897_3_, p_175897_1_ + p_175897_4_, p_175897_2_ + p_175897_5_, p_175897_0_ + p_175897_6_ - 1 + p_175897_3_, p_175897_1_ + p_175897_7_ - 1 + p_175897_4_, p_175897_2_ + p_175897_8_ - 1 + p_175897_5_);
            case WEST ->
                    new StructureBoundingBox(p_175897_0_ - p_175897_8_ + 1 + p_175897_5_, p_175897_1_ + p_175897_4_, p_175897_2_ + p_175897_3_, p_175897_0_ + p_175897_5_, p_175897_1_ + p_175897_7_ - 1 + p_175897_4_, p_175897_2_ + p_175897_6_ - 1 + p_175897_3_);
            case EAST ->
                    new StructureBoundingBox(p_175897_0_ + p_175897_5_, p_175897_1_ + p_175897_4_, p_175897_2_ + p_175897_3_, p_175897_0_ + p_175897_8_ - 1 + p_175897_5_, p_175897_1_ + p_175897_7_ - 1 + p_175897_4_, p_175897_2_ + p_175897_6_ - 1 + p_175897_3_);
            default ->
                    new StructureBoundingBox(p_175897_0_ + p_175897_3_, p_175897_1_ + p_175897_4_, p_175897_2_ + p_175897_5_, p_175897_0_ + p_175897_6_ - 1 + p_175897_3_, p_175897_1_ + p_175897_7_ - 1 + p_175897_4_, p_175897_2_ + p_175897_8_ - 1 + p_175897_5_);
        };
    }

    public static StructureBoundingBox func_175899_a(int p_175899_0_, int p_175899_1_, int p_175899_2_, int p_175899_3_, int p_175899_4_, int p_175899_5_) {
        return new StructureBoundingBox(Math.min(p_175899_0_, p_175899_3_), Math.min(p_175899_1_, p_175899_4_), Math.min(p_175899_2_, p_175899_5_), Math.max(p_175899_0_, p_175899_3_), Math.max(p_175899_1_, p_175899_4_), Math.max(p_175899_2_, p_175899_5_));
    }

    public boolean intersectsWith(StructureBoundingBox structurebb) {
        return maxX >= structurebb.minX && minX <= structurebb.maxX && maxZ >= structurebb.minZ && minZ <= structurebb.maxZ && maxY >= structurebb.minY && minY <= structurebb.maxY;
    }

    public boolean intersectsWith(int minXIn, int minZIn, int maxXIn, int maxZIn) {
        return maxX >= minXIn && minX <= maxXIn && maxZ >= minZIn && minZ <= maxZIn;
    }

    public void expandTo(StructureBoundingBox sbb) {
        minX = Math.min(minX, sbb.minX);
        minY = Math.min(minY, sbb.minY);
        minZ = Math.min(minZ, sbb.minZ);
        maxX = Math.max(maxX, sbb.maxX);
        maxY = Math.max(maxY, sbb.maxY);
        maxZ = Math.max(maxZ, sbb.maxZ);
    }

    public void offset(int x, int y, int z) {
        minX += x;
        minY += y;
        minZ += z;
        maxX += x;
        maxY += y;
        maxZ += z;
    }

    public boolean isVecInside(Vec3i vec) {
        return vec.getX() >= minX && vec.getX() <= maxX && vec.getZ() >= minZ && vec.getZ() <= maxZ && vec.getY() >= minY && vec.getY() <= maxY;
    }

    public Vec3i func_175896_b() {
        return new Vec3i(maxX - minX, maxY - minY, maxZ - minZ);
    }

    public int getXSize() {
        return maxX - minX + 1;
    }

    public int getYSize() {
        return maxY - minY + 1;
    }

    public int getZSize() {
        return maxZ - minZ + 1;
    }

    public Vec3i getCenter() {
        return new BlockPos(minX + (maxX - minX + 1) / 2, minY + (maxY - minY + 1) / 2, minZ + (maxZ - minZ + 1) / 2);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x0", minX).add("y0", minY).add("z0", minZ).add("x1", maxX).add("y1", maxY).add("z1", maxZ).toString();
    }

    public NBTTagIntArray toNBTTagIntArray() {
        return new NBTTagIntArray(new int[]{minX, minY, minZ, maxX, maxY, maxZ});
    }
}
