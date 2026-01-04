package net.minecraft.client.renderer.culling;

import org.joml.Vector4f;

public class ClippingHelper {
    public float[][] frustum = new float[6][4];
    public float[] projectionMatrix = new float[16];
    public float[] modelviewMatrix = new float[16];
    public float[] clippingMatrix = new float[16];
    public boolean disabled;

    private float dot(Vector4f plane, float x, float y, float z) {
        return plane.x * x + plane.y * y + plane.z * z + plane.w;
    }

    public boolean isBoxInFrustum(
            double x, double y, double z,
            double x2, double y2, double z2
    ) {
        if (!this.disabled) {
            float f = (float) x;
            float f1 = (float) y;
            float f2 = (float) z;
            float f3 = (float) x2;
            float f4 = (float) y2;
            float f5 = (float) z2;

            for (int i = 0; i < 6; ++i) {
                float[] plane = this.frustum[i];
                float px = plane[0];
                float py = plane[1];
                float pz = plane[2];
                float pw = plane[3];

                if (
                        px * f + py * f1 + pz * f2 + pw <= 0.0F &&
                                px * f3 + py * f1 + pz * f2 + pw <= 0.0F &&
                                px * f + py * f4 + pz * f2 + pw <= 0.0F &&
                                px * f3 + py * f4 + pz * f2 + pw <= 0.0F &&
                                px * f + py * f1 + pz * f5 + pw <= 0.0F &&
                                px * f3 + py * f1 + pz * f5 + pw <= 0.0F &&
                                px * f + py * f4 + pz * f5 + pw <= 0.0F &&
                                px * f3 + py * f4 + pz * f5 + pw <= 0.0F
                ) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBoxInFrustumFully(
            double x, double y, double z,
            double x2, double y2, double z2
    ) {
        if (!this.disabled) {
            float f = (float) x;
            float f1 = (float) y;
            float f2 = (float) z;
            float f3 = (float) x2;
            float f4 = (float) y2;
            float f5 = (float) z2;

            for (int i = 0; i < 6; ++i) {
                float[] plane = this.frustum[i];
                float px = plane[0];
                float py = plane[1];
                float pz = plane[2];
                float pw = plane[3];

                if (i < 4) {
                    if (
                            px * f + py * f1 + pz * f2 + pw <= 0.0F ||
                                    px * f3 + py * f1 + pz * f2 + pw <= 0.0F ||
                                    px * f + py * f4 + pz * f2 + pw <= 0.0F ||
                                    px * f3 + py * f4 + pz * f2 + pw <= 0.0F ||
                                    px * f + py * f1 + pz * f5 + pw <= 0.0F ||
                                    px * f3 + py * f1 + pz * f5 + pw <= 0.0F ||
                                    px * f + py * f4 + pz * f5 + pw <= 0.0F ||
                                    px * f3 + py * f4 + pz * f5 + pw <= 0.0F
                    ) {
                        return false;
                    }
                } else if (
                        px * f + py * f1 + pz * f2 + pw <= 0.0F &&
                                px * f3 + py * f1 + pz * f2 + pw <= 0.0F &&
                                px * f + py * f4 + pz * f2 + pw <= 0.0F &&
                                px * f3 + py * f4 + pz * f2 + pw <= 0.0F &&
                                px * f + py * f1 + pz * f5 + pw <= 0.0F &&
                                px * f3 + py * f1 + pz * f5 + pw <= 0.0F &&
                                px * f + py * f4 + pz * f5 + pw <= 0.0F &&
                                px * f3 + py * f4 + pz * f5 + pw <= 0.0F
                ) {
                    return false;
                }
            }

        }
        return true;
    }
}
