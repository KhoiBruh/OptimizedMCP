package net.minecraft.client.renderer.culling;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.system.MemoryStack;

public class ClippingHelperImpl extends ClippingHelper {
    private static final ClippingHelperImpl instance = new ClippingHelperImpl();

    public static ClippingHelper getInstance() {
        instance.init();
        return instance;
    }

    public void init() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var projectionBuffer = stack.mallocFloat(16);
            var modelviewBuffer = stack.mallocFloat(16);

            GlStateManager.getFloat(2983, projectionBuffer);
            GlStateManager.getFloat(2982, modelviewBuffer);

            projectionBuffer.flip();
            modelviewBuffer.flip();

            projection.set(projectionBuffer);
            modelview.set(modelviewBuffer);

            clipping.set(projection).mul(modelview);

            frustum.set(clipping);
        }
    }
}
