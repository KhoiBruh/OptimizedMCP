package net.minecraft.client.renderer.culling;

import net.minecraft.util.AxisAlignedBB;

public class Frustum implements ICamera {
    private final ClippingHelper clippingHelper;
    private double xPosition;
    private double yPosition;
    private double zPosition;

    public Frustum() {
        this(ClippingHelperImpl.getInstance());
    }

    public Frustum(ClippingHelper clippingHelper) {
        this.clippingHelper = clippingHelper;
    }

    public void setPosition(double x, double y, double z) {
        xPosition = x;
        yPosition = y;
        zPosition = z;
    }

    public boolean isBoxInFrustum(double x, double y, double z, double x2, double y2, double z2) {
        return clippingHelper.isBoxInFrustum(x - xPosition, y - yPosition, z - zPosition, x2 - xPosition, y2 - yPosition, z2 - zPosition);
    }

    public boolean isBoundingBoxInFrustum(AxisAlignedBB axisAlignedBB) {
        return isBoxInFrustum(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    }

    public boolean isBoxInFrustumFully(double x, double y, double z, double x2, double y2, double z2) {
        return clippingHelper.isBoxInFrustumFully(x - xPosition, y - yPosition, z - zPosition, x2 - xPosition, y2 - yPosition, z2 - zPosition);
    }
}
