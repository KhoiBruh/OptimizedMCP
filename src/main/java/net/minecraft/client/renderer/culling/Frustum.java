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
        this.xPosition = x;
        this.yPosition = y;
        this.zPosition = z;
    }

    public boolean isBoxInFrustum(double x, double y, double z, double x2, double y2, double z2) {
        return this.clippingHelper.isBoxInFrustum(x - this.xPosition, y - this.yPosition, z - this.zPosition, x2 - this.xPosition, y2 - this.yPosition, z2 - this.zPosition);
    }

    public boolean isBoundingBoxInFrustum(AxisAlignedBB axisAlignedBB) {
        return this.isBoxInFrustum(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    }

    public boolean isBoxInFrustumFully(double x, double y, double z, double x2, double y2, double z2) {
        return this.clippingHelper.isBoxInFrustumFully(x - this.xPosition, y - this.yPosition, z - this.zPosition, x2 - this.xPosition, y2 - this.yPosition, z2 - this.zPosition);
    }
}
