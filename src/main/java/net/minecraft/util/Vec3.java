package net.minecraft.util;

public record Vec3(double xCoord, double yCoord, double zCoord) {
    public Vec3 {
        if (xCoord == -0.0D) {
            xCoord = 0.0D;
        }

        if (yCoord == -0.0D) {
            yCoord = 0.0D;
        }

        if (zCoord == -0.0D) {
            zCoord = 0.0D;
        }

    }

    public Vec3(Vec3i p_i46377_1_) {
        this(p_i46377_1_.getX(), p_i46377_1_.getY(), p_i46377_1_.getZ());
    }

    public Vec3 subtractReverse(Vec3 vec) {
        return new Vec3(vec.xCoord - xCoord, vec.yCoord - yCoord, vec.zCoord - zCoord);
    }

    public Vec3 normalize() {
        double d0 = MathHelper.sqrt(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
        return d0 < 1.0E-4D ? new Vec3(0.0D, 0.0D, 0.0D) : new Vec3(xCoord / d0, yCoord / d0, zCoord / d0);
    }

    public double dotProduct(Vec3 vec) {
        return xCoord * vec.xCoord + yCoord * vec.yCoord + zCoord * vec.zCoord;
    }

    public Vec3 crossProduct(Vec3 vec) {
        return new Vec3(yCoord * vec.zCoord - zCoord * vec.yCoord, zCoord * vec.xCoord - xCoord * vec.zCoord, xCoord * vec.yCoord - yCoord * vec.xCoord);
    }

    public Vec3 subtract(Vec3 vec) {
        return subtract(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3 subtract(double x, double y, double z) {
        return addVector(-x, -y, -z);
    }

    public Vec3 add(Vec3 vec) {
        return addVector(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3 addVector(double x, double y, double z) {
        return new Vec3(xCoord + x, yCoord + y, zCoord + z);
    }

    public double distanceTo(Vec3 vec) {
        double d0 = vec.xCoord - xCoord;
        double d1 = vec.yCoord - yCoord;
        double d2 = vec.zCoord - zCoord;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double squareDistanceTo(Vec3 vec) {
        double d0 = vec.xCoord - xCoord;
        double d1 = vec.yCoord - yCoord;
        double d2 = vec.zCoord - zCoord;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double lengthVector() {
        return MathHelper.sqrt(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
    }

    public Vec3 getIntermediateWithXValue(Vec3 vec, double x) {
        double d0 = vec.xCoord - xCoord;
        double d1 = vec.yCoord - yCoord;
        double d2 = vec.zCoord - zCoord;

        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (x - xCoord) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(xCoord + d0 * d3, yCoord + d1 * d3, zCoord + d2 * d3) : null;
        }
    }

    public Vec3 getIntermediateWithYValue(Vec3 vec, double y) {
        double d0 = vec.xCoord - xCoord;
        double d1 = vec.yCoord - yCoord;
        double d2 = vec.zCoord - zCoord;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (y - yCoord) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(xCoord + d0 * d3, yCoord + d1 * d3, zCoord + d2 * d3) : null;
        }
    }

    public Vec3 getIntermediateWithZValue(Vec3 vec, double z) {
        double d0 = vec.xCoord - xCoord;
        double d1 = vec.yCoord - yCoord;
        double d2 = vec.zCoord - zCoord;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (z - zCoord) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(xCoord + d0 * d3, yCoord + d1 * d3, zCoord + d2 * d3) : null;
        }
    }

    public String toString() {
        return "(" + xCoord + ", " + yCoord + ", " + zCoord + ")";
    }

    public Vec3 rotatePitch(float pitch) {
        float f = MathHelper.cos(pitch);
        float f1 = MathHelper.sin(pitch);
        double d1 = yCoord * (double) f + zCoord * (double) f1;
        double d2 = zCoord * (double) f - yCoord * (double) f1;
        return new Vec3(xCoord, d1, d2);
    }

    public Vec3 rotateYaw(float yaw) {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double d0 = xCoord * (double) f + zCoord * (double) f1;
        double d2 = zCoord * (double) f - xCoord * (double) f1;
        return new Vec3(d0, yCoord, d2);
    }
}
