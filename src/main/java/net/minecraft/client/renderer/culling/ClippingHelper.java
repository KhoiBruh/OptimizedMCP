package net.minecraft.client.renderer.culling;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

public class ClippingHelper {
    public FrustumIntersection frustum = new FrustumIntersection();
    public Matrix4f projectionMatrix = new Matrix4f();
    public Matrix4f modelviewMatrix = new Matrix4f();
    public Matrix4f clippingMatrix = new Matrix4f();
    public boolean disabled;

    public boolean isBoxInFrustum(double x, double y, double z, double x2, double y2, double z2) {
        if (disabled) return true;
        return frustum.testAab((float) x, (float) y, (float) z, (float) x2, (float) y2, (float) z2);
    }

    public boolean isBoxInFrustumFully(double x, double y, double z, double x2, double y2, double z2) {
        if (disabled) return true;
        return frustum.intersectAab((float) x, (float) y, (float) z, (float) x2, (float) y2, (float) z2) == FrustumIntersection.INSIDE;
    }
}
