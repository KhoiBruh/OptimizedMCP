package net.optifine.util;

import java.lang.reflect.Array;
import java.util.ArrayDeque;

public class ArrayCache {
    private final Class<?> elementClass;
    private final int maxCacheSize;
    private final ArrayDeque<Object> cache = new ArrayDeque<>();

    public ArrayCache(Class<?> elementClass, int maxCacheSize) {
        this.elementClass = elementClass;
        this.maxCacheSize = maxCacheSize;
    }

    public synchronized Object allocate(int size) {
        Object object = cache.pollLast();

        if (object == null || Array.getLength(object) < size) {
            object = Array.newInstance(elementClass, size);
        }

        return object;
    }

    public synchronized void free(Object arr) {
        if (arr != null) {
            Class<?> oclass = arr.getClass();

            if (oclass.getComponentType() != elementClass) {
                throw new IllegalArgumentException("Wrong component type");
            } else if (cache.size() < maxCacheSize) {
                cache.add(arr);
            }
        }
    }
}
