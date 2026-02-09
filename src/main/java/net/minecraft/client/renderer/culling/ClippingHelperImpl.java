package net.minecraft.client.renderer.culling;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;

import java.nio.FloatBuffer;

public class ClippingHelperImpl extends ClippingHelper {
    private static final ClippingHelperImpl instance = new ClippingHelperImpl();
    private final FloatBuffer projectionMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer modelviewMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);

    private final float[] projectionArray = new float[16];
    private final float[] modelviewArray = new float[16];

    /**
     * Initialises the ClippingHelper object then returns an instance of it.
     */
    public static ClippingHelper getInstance() {
        instance.init();
        return instance;
    }

    public void init() {
        projectionMatrixBuffer.clear();
        modelviewMatrixBuffer.clear();

        GlStateManager.getFloat(2983, projectionMatrixBuffer);
        GlStateManager.getFloat(2982, modelviewMatrixBuffer);

        projectionMatrixBuffer.flip().limit(16);
        projectionMatrixBuffer.get(projectionArray);

        modelviewMatrixBuffer.flip().limit(16);
        modelviewMatrixBuffer.get(modelviewArray);

        projectionMatrix.set(projectionArray);
        modelviewMatrix.set(modelviewArray);

        clippingMatrix.set(projectionMatrix).mul(modelviewMatrix);

        frustum.set(clippingMatrix);
    }
}
