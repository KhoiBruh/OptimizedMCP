package net.optifine.util;

import java.util.*;

public class PropertiesOrdered extends Properties {
    private final Set<Object> keysOrdered = new LinkedHashSet<>();

    public synchronized Object put(Object key, Object value) {
        keysOrdered.add(key);
        return super.put(key, value);
    }

    public Set<Object> keySet() {
        Set<Object> set = super.keySet();
        keysOrdered.retainAll(set);
        return Collections.unmodifiableSet(keysOrdered);
    }

    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(keySet());
    }
}
