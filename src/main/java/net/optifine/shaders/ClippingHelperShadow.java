package net.optifine.shaders;

import net.minecraft.client.renderer.culling.ClippingHelper;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ClippingHelperShadow extends ClippingHelper {
    private static final ClippingHelperShadow instance = new ClippingHelperShadow();
    
    // Own frustum planes for shadow calculations (not inherited from ClippingHelper anymore)
    private final Vector4f[] frustumPlanes = new Vector4f[6];
    private final Vector4f[] shadowClipPlanes = new Vector4f[10];
    private final Vector3f vecIntersection = new Vector3f();
    private int shadowClipPlaneCount;

    public ClippingHelperShadow() {
        for (int i = 0; i < frustumPlanes.length; ++i)
            frustumPlanes[i] = new Vector4f();
        for (int i = 0; i < shadowClipPlanes.length; ++i)
            shadowClipPlanes[i] = new Vector4f();
    }

    public static ClippingHelper getInstance() {
        instance.init();
        return instance;
    }

    @Override
    public boolean isBoxInFrustum(double x1, double y1, double z1, double x2, double y2, double z2) {
        for (int i = 0; i < shadowClipPlaneCount; ++i) {
            Vector4f plane = shadowClipPlanes[i];
            if (plane.dot((float) x1, (float) y1, (float) z1, 1.0f) <= 0.0F &&
                    plane.dot((float) x2, (float) y1, (float) z1, 1.0f) <= 0.0F &&
                    plane.dot((float) x1, (float) y2, (float) z1, 1.0f) <= 0.0F &&
                    plane.dot((float) x2, (float) y2, (float) z1, 1.0f) <= 0.0F &&
                    plane.dot((float) x1, (float) y1, (float) z2, 1.0f) <= 0.0F &&
                    plane.dot((float) x2, (float) y1, (float) z2, 1.0f) <= 0.0F &&
                    plane.dot((float) x1, (float) y2, (float) z2, 1.0f) <= 0.0F &&
                    plane.dot((float) x2, (float) y2, (float) z2, 1.0f) <= 0.0F)
                return false;
        }
        return true;
    }

    private void normalizePlane(Vector4f plane) {
        float length = (float) Math.sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
        plane.mul(1.0f / length);
    }

    private void makeShadowPlane(Vector4f shadowPlane, Vector4f positivePlane, Vector4f negativePlane, Vector3f vecSun) {
        vecIntersection.set(positivePlane.x, positivePlane.y, positivePlane.z)
                .cross(negativePlane.x, negativePlane.y, negativePlane.z);

        Vector3f tempCross = new Vector3f(vecIntersection).cross(vecSun);
        shadowPlane.set(tempCross.x, tempCross.y, tempCross.z, 0.0f);

        normalizePlane(shadowPlane);

        float f = positivePlane.x * negativePlane.x + positivePlane.y * negativePlane.y + positivePlane.z * negativePlane.z;
        float f1 = shadowPlane.x * negativePlane.x + shadowPlane.y * negativePlane.y + shadowPlane.z * negativePlane.z;
        
        float f2 = distance(shadowPlane.x, shadowPlane.y, shadowPlane.z, negativePlane.x * f1, negativePlane.y * f1, negativePlane.z * f1);
        float f3 = distance(positivePlane.x, positivePlane.y, positivePlane.z, negativePlane.x * f, negativePlane.y * f, negativePlane.z * f);
        float f4 = f2 / f3;
        
        float f5 = shadowPlane.x * positivePlane.x + shadowPlane.y * positivePlane.y + shadowPlane.z * positivePlane.z;
        float f6 = distance(shadowPlane.x, shadowPlane.y, shadowPlane.z, positivePlane.x * f5, positivePlane.y * f5, positivePlane.z * f5);
        float f7 = distance(negativePlane.x, negativePlane.y, negativePlane.z, positivePlane.x * f, positivePlane.y * f, positivePlane.z * f);
        float f8 = f6 / f7;
        
        shadowPlane.w = positivePlane.w * f4 + negativePlane.w * f8;
    }

    private float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return length(x1 - x2, y1 - y2, z1 - z2);
    }
    
    private float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public void init() {
        // Set matrices from Shaders float arrays
        projectionMatrix.set(Shaders.faProjection);
        modelviewMatrix.set(Shaders.faModelView);
        
        // clippingMatrix = projection * modelview
        clippingMatrix.set(projectionMatrix).mul(modelviewMatrix);
        
        // Update FrustumIntersection (inherited from ClippingHelper)
        frustum.set(clippingMatrix);
        
        // Extract planes locally for shadow calculations (OptiFine Order: Right, Left, Bottom, Top, Far, Near)
        frustumPlanes[0].set(clippingMatrix.m03() - clippingMatrix.m00(), clippingMatrix.m13() - clippingMatrix.m10(), clippingMatrix.m23() - clippingMatrix.m20(), clippingMatrix.m33() - clippingMatrix.m30());
        normalizePlane(frustumPlanes[0]);
        
        frustumPlanes[1].set(clippingMatrix.m03() + clippingMatrix.m00(), clippingMatrix.m13() + clippingMatrix.m10(), clippingMatrix.m23() + clippingMatrix.m20(), clippingMatrix.m33() + clippingMatrix.m30());
        normalizePlane(frustumPlanes[1]);
        
        frustumPlanes[2].set(clippingMatrix.m03() + clippingMatrix.m01(), clippingMatrix.m13() + clippingMatrix.m11(), clippingMatrix.m23() + clippingMatrix.m21(), clippingMatrix.m33() + clippingMatrix.m31());
        normalizePlane(frustumPlanes[2]);
        
        frustumPlanes[3].set(clippingMatrix.m03() - clippingMatrix.m01(), clippingMatrix.m13() - clippingMatrix.m11(), clippingMatrix.m23() - clippingMatrix.m21(), clippingMatrix.m33() - clippingMatrix.m31());
        normalizePlane(frustumPlanes[3]);
        
        frustumPlanes[4].set(clippingMatrix.m03() - clippingMatrix.m02(), clippingMatrix.m13() - clippingMatrix.m12(), clippingMatrix.m23() - clippingMatrix.m22(), clippingMatrix.m33() - clippingMatrix.m32());
        normalizePlane(frustumPlanes[4]);
        
        frustumPlanes[5].set(clippingMatrix.m03() + clippingMatrix.m02(), clippingMatrix.m13() + clippingMatrix.m12(), clippingMatrix.m23() + clippingMatrix.m22(), clippingMatrix.m33() + clippingMatrix.m32());
        normalizePlane(frustumPlanes[5]);

        float[] lightPos = Shaders.shadowLightPositionVector;
        Vector3f vecSun = new Vector3f(lightPos[0], lightPos[1], lightPos[2]);

        float f = frustumPlanes[0].x * vecSun.x + frustumPlanes[0].y * vecSun.y + frustumPlanes[0].z * vecSun.z;
        float f1 = frustumPlanes[1].x * vecSun.x + frustumPlanes[1].y * vecSun.y + frustumPlanes[1].z * vecSun.z;
        float f2 = frustumPlanes[2].x * vecSun.x + frustumPlanes[2].y * vecSun.y + frustumPlanes[2].z * vecSun.z;
        float f3 = frustumPlanes[3].x * vecSun.x + frustumPlanes[3].y * vecSun.y + frustumPlanes[3].z * vecSun.z;
        float f4 = frustumPlanes[4].x * vecSun.x + frustumPlanes[4].y * vecSun.y + frustumPlanes[4].z * vecSun.z;
        float f5 = frustumPlanes[5].x * vecSun.x + frustumPlanes[5].y * vecSun.y + frustumPlanes[5].z * vecSun.z;
        
        shadowClipPlaneCount = 0;

        if (f >= 0.0F) {
            shadowClipPlanes[shadowClipPlaneCount++].set(frustumPlanes[0]);
            if (f > 0.0F) {
                if (f2 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[0], frustumPlanes[2], vecSun);
                if (f3 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[0], frustumPlanes[3], vecSun);
                if (f4 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[0], frustumPlanes[4], vecSun);
                if (f5 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[0], frustumPlanes[5], vecSun);
            }
        }
        
        if (f1 >= 0.0F) {
            shadowClipPlanes[shadowClipPlaneCount++].set(frustumPlanes[1]);
            if (f1 > 0.0F) {
                if (f2 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[1], frustumPlanes[2], vecSun);
                if (f3 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[1], frustumPlanes[3], vecSun);
                if (f4 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[1], frustumPlanes[4], vecSun);
                if (f5 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[1], frustumPlanes[5], vecSun);
            }
        }
        
        if (f2 >= 0.0F) {
            shadowClipPlanes[shadowClipPlaneCount++].set(frustumPlanes[2]);
            if (f2 > 0.0F) {
                if (f < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[2], frustumPlanes[0], vecSun);
                if (f1 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[2], frustumPlanes[1], vecSun);
                if (f4 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[2], frustumPlanes[4], vecSun);
                if (f5 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[2], frustumPlanes[5], vecSun);
            }
        }

        if (f3 >= 0.0F) {
            shadowClipPlanes[shadowClipPlaneCount++].set(frustumPlanes[3]);
            if (f3 > 0.0F) {
                if (f < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[3], frustumPlanes[0], vecSun);
                if (f1 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[3], frustumPlanes[1], vecSun);
                if (f4 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[3], frustumPlanes[4], vecSun);
                if (f5 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[3], frustumPlanes[5], vecSun);
            }
        }

        if (f4 >= 0.0F) {
            shadowClipPlanes[shadowClipPlaneCount++].set(frustumPlanes[4]);
            if (f4 > 0.0F) {
                if (f < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[4], frustumPlanes[0], vecSun);
                if (f1 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[4], frustumPlanes[1], vecSun);
                if (f2 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[4], frustumPlanes[2], vecSun);
                if (f3 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[4], frustumPlanes[3], vecSun);
            }
        }

        if (f5 >= 0.0F) {
            shadowClipPlanes[shadowClipPlaneCount++].set(frustumPlanes[5]);
            if (f5 > 0.0F) {
                if (f < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[5], frustumPlanes[0], vecSun);
                if (f1 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[5], frustumPlanes[1], vecSun);
                if (f2 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[5], frustumPlanes[2], vecSun);
                if (f3 < 0.0F) makeShadowPlane(shadowClipPlanes[shadowClipPlaneCount++], frustumPlanes[5], frustumPlanes[3], vecSun);
            }
        }
    }
}
