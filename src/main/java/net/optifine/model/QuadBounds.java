package net.optifine.model;

import net.minecraft.util.EnumFacing;

public class QuadBounds {
    private float minX = Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE;
    private float minZ = Float.MAX_VALUE;
    private float maxX = -3.4028235E38F;
    private float maxY = -3.4028235E38F;
    private float maxZ = -3.4028235E38F;

    public QuadBounds(int[] vertexData) {
        int i = vertexData.length / 4;

        for (int j = 0; j < 4; ++j) {
            int k = j * i;
            float f = Float.intBitsToFloat(vertexData[k]);
            float f1 = Float.intBitsToFloat(vertexData[k + 1]);
            float f2 = Float.intBitsToFloat(vertexData[k + 2]);

            if (minX > f) {
                minX = f;
            }

            if (minY > f1) {
                minY = f1;
            }

            if (minZ > f2) {
                minZ = f2;
            }

            if (maxX < f) {
                maxX = f;
            }

            if (maxY < f1) {
                maxY = f1;
            }

            if (maxZ < f2) {
                maxZ = f2;
            }
        }
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMinZ() {
        return minZ;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMaxZ() {
        return maxZ;
    }

    public boolean isFaceQuad(EnumFacing face) {
        float f;
        float f1;
        float f2;

        switch (face) {
            case DOWN:
                f = minY;
                f1 = maxY;
                f2 = 0.0F;
                break;

            case UP:
                f = minY;
                f1 = maxY;
                f2 = 1.0F;
                break;

            case NORTH:
                f = minZ;
                f1 = maxZ;
                f2 = 0.0F;
                break;

            case SOUTH:
                f = minZ;
                f1 = maxZ;
                f2 = 1.0F;
                break;

            case WEST:
                f = minX;
                f1 = maxX;
                f2 = 0.0F;
                break;

            case EAST:
                f = minX;
                f1 = maxX;
                f2 = 1.0F;
                break;

            default:
                return false;
        }

        return f == f2 && f1 == f2;
    }

    public boolean isFullQuad(EnumFacing face) {
        float f;
        float f1;
        float f2;
        float f3;

        switch (face) {
            case DOWN:
            case UP:
                f = minX;
                f1 = maxX;
                f2 = minZ;
                f3 = maxZ;
                break;

            case NORTH:
            case SOUTH:
                f = minX;
                f1 = maxX;
                f2 = minY;
                f3 = maxY;
                break;

            case WEST:
            case EAST:
                f = minY;
                f1 = maxY;
                f2 = minZ;
                f3 = maxZ;
                break;

            default:
                return false;
        }

        return f == 0.0F && f1 == 1.0F && f2 == 0.0F && f3 == 1.0F;
    }
}
