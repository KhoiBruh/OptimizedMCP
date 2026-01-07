package net.optifine.util;

import net.minecraft.src.Config;

import java.lang.reflect.Method;

public class NativeMemory {
    private static final LongSupplier bufferAllocatedSupplier = makeLongSupplier(new String[][]{{"getDirectBufferPool", "getMemoryUsed"}, {"getDirectBufferPool", "getMemoryUsed"}});
    private static final LongSupplier bufferMaximumSupplier = makeLongSupplier(new String[][]{{"maxDirectMemory"}, {"jdk.internal.misc.VM", "maxDirectMemory"}});

    public static long getBufferAllocated() {
        return bufferAllocatedSupplier == null ? -1L : bufferAllocatedSupplier.getAsLong();
    }

    public static long getBufferMaximum() {
        return bufferMaximumSupplier == null ? -1L : bufferMaximumSupplier.getAsLong();
    }

    private static LongSupplier makeLongSupplier(String[][] paths) {
        for (String[] astring : paths) {
            try {
                return makeLongSupplier(astring);
            } catch (Throwable throwable) {
                Config.warn(throwable.getClass().getName() + ": " + throwable.getMessage());
            }
        }

        return null;
    }

    private static LongSupplier makeLongSupplier(String[] path) throws Exception {
        if (path.length < 2) {
            return null;
        } else {
            Class<?> oclass = Class.forName(path[0]);
            Method method = oclass.getMethod(path[1]);
            method.setAccessible(true);
            Object object = null;

            for (int i = 2; i < path.length; ++i) {
                String s = path[i];
                object = method.invoke(object);
                method = object.getClass().getMethod(s);
                method.setAccessible(true);
            }

            Method finalMethod = method;
            Object finalObject = object;
            return new LongSupplier() {
                private boolean disabled = false;

                public long getAsLong() {
                    if (disabled) {
                        return -1L;
                    } else {
                        try {
                            return (Long) finalMethod.invoke(finalObject, new Object[0]);
                        } catch (Throwable throwable) {
                            Config.warn(throwable.getClass().getName() + ": " + throwable.getMessage());
                            disabled = true;
                            return -1L;
                        }
                    }
                }
            };
        }
    }
}
