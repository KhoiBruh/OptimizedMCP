package net.minecraft.util;

import java.util.Objects;

public class RegistryNamespacedDefaultedByKey<K, V> extends RegistryNamespaced<K, V> {
    private final K defaultValueKey;
    private V defaultValue;

    public RegistryNamespacedDefaultedByKey(K defaultValueKeyIn) {
        defaultValueKey = defaultValueKeyIn;
    }

    public void register(int id, K key, V value) {
        if (defaultValueKey.equals(key)) {
            defaultValue = value;
        }

        super.register(id, key, value);
    }

    public void validateKey() {
        Objects.requireNonNull(defaultValueKey);
    }

    public V getObject(K name) {
        V v = super.getObject(name);
        return v == null ? defaultValue : v;
    }

    public V getObjectById(int id) {
        V v = super.getObjectById(id);
        return v == null ? defaultValue : v;
    }
}
