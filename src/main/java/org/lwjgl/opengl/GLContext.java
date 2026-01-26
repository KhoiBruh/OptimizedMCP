package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextCapabilities;

import javax.annotation.Nullable;

public class GLContext {
    private static final ThreadLocal<ContextCapabilities> current_capabilities = new ThreadLocal<>();

    public static ContextCapabilities getCapabilities() {
        ContextCapabilities caps = getCapabilitiesImpl();
        if (null == caps) {
            //throw new RuntimeException("No OpenGL context found in the current thread.");
            try {
                ContextCapabilities created = new ContextCapabilities(false);
                setCapabilities(created);
                return created;
            } catch (LWJGLException e) {
                throw new RuntimeException("No OpenGL context found in the current thread and could not create!", e);
            }
        }

        return caps;
    }

    static void setCapabilities(ContextCapabilities capabilities) {
        current_capabilities.set(capabilities);
    }

    private static @Nullable ContextCapabilities getCapabilitiesImpl() {
        return getThreadLocalCapabilities();
    }

    private static @Nullable ContextCapabilities getThreadLocalCapabilities() {
        return current_capabilities.get();
    }
}
