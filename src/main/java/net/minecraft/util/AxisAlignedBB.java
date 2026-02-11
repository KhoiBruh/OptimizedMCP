package net.minecraft.util;

public class AxisAlignedBB {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);
    }

    public AxisAlignedBB(BlockPos pos1, BlockPos pos2) {
        minX = pos1.getX();
        minY = pos1.getY();
        minZ = pos1.getZ();
        maxX = pos2.getX();
        maxY = pos2.getY();
        maxZ = pos2.getZ();
    }

    public static AxisAlignedBB fromBounds(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = Math.min(x1, x2);
        double d1 = Math.min(y1, y2);
        double d2 = Math.min(z1, z2);
        double d3 = Math.max(x1, x2);
        double d4 = Math.max(y1, y2);
        double d5 = Math.max(z1, z2);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB addCoord(double x, double y, double z) {
        double d0 = minX;
        double d1 = minY;
        double d2 = minZ;
        double d3 = maxX;
        double d4 = maxY;
        double d5 = maxZ;

        if (x < 0.0D) {
            d0 += x;
        } else if (x > 0.0D) {
            d3 += x;
        }

        if (y < 0.0D) {
            d1 += y;
        } else if (y > 0.0D) {
            d4 += y;
        }

        if (z < 0.0D) {
            d2 += z;
        } else if (z > 0.0D) {
            d5 += z;
        }

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB expand(double x, double y, double z) {
        double d0 = minX - x;
        double d1 = minY - y;
        double d2 = minZ - z;
        double d3 = maxX + x;
        double d4 = maxY + y;
        double d5 = maxZ + z;
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB union(AxisAlignedBB other) {
        double minX = Math.min(this.minX, other.minX);
        double minY = Math.min(this.minY, other.minY);
        double minZ = Math.min(this.minZ, other.minZ);
        double maxX = Math.max(this.maxX, other.maxX);
        double maxY = Math.max(this.maxY, other.maxY);
        double maxZ = Math.max(this.maxZ, other.maxZ);
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB offset(double x, double y, double z) {
        return new AxisAlignedBB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        if (other.maxY > minY && other.minY < maxY && other.maxZ > minZ && other.minZ < maxZ) {
            if (offsetX > 0.0D && other.maxX <= minX) {
                double d1 = minX - other.maxX;

                if (d1 < offsetX) offsetX = d1;
            } else if (offsetX < 0.0D && other.minX >= maxX) {
                double d0 = maxX - other.minX;

                if (d0 > offsetX) offsetX = d0;
            }
        }

        return offsetX;
    }

    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        if (other.maxX > minX && other.minX < maxX && other.maxZ > minZ && other.minZ < maxZ) {
            if (offsetY > 0.0D && other.maxY <= minY) {
                double d1 = minY - other.maxY;

                if (d1 < offsetY) offsetY = d1;
            } else if (offsetY < 0.0D && other.minY >= maxY) {
                double d0 = maxY - other.minY;

                if (d0 > offsetY) offsetY = d0;
            }
        }

        return offsetY;
    }

    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        if (other.maxX > minX && other.minX < maxX && other.maxY > minY && other.minY < maxY) {
            if (offsetZ > 0.0D && other.maxZ <= minZ) {
                double d1 = minZ - other.maxZ;

                if (d1 < offsetZ) offsetZ = d1;
            } else if (offsetZ < 0.0D && other.minZ >= maxZ) {
                double d0 = maxZ - other.minZ;

                if (d0 > offsetZ) offsetZ = d0;
            }
        }

        return offsetZ;
    }

    public boolean intersectsWith(AxisAlignedBB other) {
        return other.maxX > minX && other.minX < maxX && (other.maxY > minY && other.minY < maxY && other.maxZ > minZ && other.minZ < maxZ);
    }

    public boolean isVecInside(Vec3 vec) {
        return vec.xCoord() > minX && vec.xCoord() < maxX && (vec.yCoord() > minY && vec.yCoord() < maxY && vec.zCoord() > minZ && vec.zCoord() < maxZ);
    }

    public double getAverageEdgeLength() {
        double d0 = maxX - minX;
        double d1 = maxY - minY;
        double d2 = maxZ - minZ;
        return (d0 + d1 + d2) / 3.0D;
    }

    public AxisAlignedBB contract(double x, double y, double z) {
        double d0 = minX + x;
        double d1 = minY + y;
        double d2 = minZ + z;
        double d3 = maxX - x;
        double d4 = maxY - y;
        double d5 = maxZ - z;
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public MovingObjectPosition calculateIntercept(Vec3 vecA, Vec3 vecB) {
        Vec3 minX = vecA.getIntermediateWithXValue(vecB, this.minX);
        Vec3 maxX = vecA.getIntermediateWithXValue(vecB, this.maxX);
        Vec3 minY = vecA.getIntermediateWithYValue(vecB, this.minY);
        Vec3 maxY = vecA.getIntermediateWithYValue(vecB, this.maxY);
        Vec3 minZ = vecA.getIntermediateWithZValue(vecB, this.minZ);
        Vec3 maxZ = vecA.getIntermediateWithZValue(vecB, this.maxZ);

        if (!isVecInYZ(minX)) minX = null;
        if (!isVecInYZ(maxX)) maxX = null;
        if (!isVecInXZ(minY)) minY = null;
        if (!isVecInXZ(maxY)) maxY = null;
        if (!isVecInXY(minZ)) minZ = null;
        if (!isVecInXY(maxZ)) maxZ = null;

        Vec3 dir = null;

        if (minX != null) dir = minX;
        if (maxX != null && (dir == null || vecA.squareDistanceTo(maxX) < vecA.squareDistanceTo(dir))) dir = maxX;
        if (minY != null && (dir == null || vecA.squareDistanceTo(minY) < vecA.squareDistanceTo(dir))) dir = minY;
        if (maxY != null && (dir == null || vecA.squareDistanceTo(maxY) < vecA.squareDistanceTo(dir))) dir = maxY;
        if (minZ != null && (dir == null || vecA.squareDistanceTo(minZ) < vecA.squareDistanceTo(dir))) dir = minZ;
        if (maxZ != null && (dir == null || vecA.squareDistanceTo(maxZ) < vecA.squareDistanceTo(dir))) dir = maxZ;

        if (dir != null) {
            Direction enumfacing;

            if (dir == minX) {
                enumfacing = Direction.WEST;
            } else if (dir == maxX) {
                enumfacing = Direction.EAST;
            } else if (dir == minY) {
                enumfacing = Direction.DOWN;
            } else if (dir == maxY) {
                enumfacing = Direction.UP;
            } else if (dir == minZ) {
                enumfacing = Direction.NORTH;
            } else {
                enumfacing = Direction.SOUTH;
            }

            return new MovingObjectPosition(dir, enumfacing);
        }

        return null;
    }

    private boolean isVecInYZ(Vec3 vec) {
        return vec != null && vec.yCoord() >= minY && vec.yCoord() <= maxY && vec.zCoord() >= minZ && vec.zCoord() <= maxZ;
    }

    private boolean isVecInXZ(Vec3 vec) {
        return vec != null && vec.xCoord() >= minX && vec.xCoord() <= maxX && vec.zCoord() >= minZ && vec.zCoord() <= maxZ;
    }

    private boolean isVecInXY(Vec3 vec) {
        return vec != null && vec.xCoord() >= minX && vec.xCoord() <= maxX && vec.yCoord() >= minY && vec.yCoord() <= maxY;
    }

    public String toString() {
        return "box[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", " + maxZ + "]";
    }

    public boolean hasNaN() {
        return Double.isNaN(minX) || Double.isNaN(minY) || Double.isNaN(minZ) || Double.isNaN(maxX) || Double.isNaN(maxY) || Double.isNaN(maxZ);
    }
}
