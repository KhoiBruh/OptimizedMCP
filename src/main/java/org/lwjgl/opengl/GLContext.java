package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;

import javax.annotation.Nullable;

public class GLContext {
    private static final ThreadLocal<ContextCapabilities> CURRENT_CAPABILITIES = new ThreadLocal<>();

    public static ContextCapabilities getCapabilities() {
        ContextCapabilities caps = getCapabilitiesImpl();

        if (caps == null) {
            try {
                ContextCapabilities created = new ContextCapabilities();
                setCapabilities(created);
                return created;
            } catch (LWJGLException e) {
                throw new RuntimeException("No OpenGL context found in the current thread and could not create!", e);
            }
        }

        return caps;
    }

    static void setCapabilities(ContextCapabilities capabilities) {
        CURRENT_CAPABILITIES.set(capabilities);
    }

    private static @Nullable ContextCapabilities getCapabilitiesImpl() {
        return getThreadLocalCapabilities();
    }

    private static @Nullable ContextCapabilities getThreadLocalCapabilities() {
        return CURRENT_CAPABILITIES.get();
    }
}
