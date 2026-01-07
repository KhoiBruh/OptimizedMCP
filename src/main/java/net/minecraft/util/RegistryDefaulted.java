package net.minecraft.util;

public class RegistryDefaulted<K, V> extends RegistrySimple<K, V> {
    private final V defaultObject;

    public RegistryDefaulted(V defaultObjectIn) {
        defaultObject = defaultObjectIn;
    }

    public V getObject(K name) {
        V v = super.getObject(name);
        return v == null ? defaultObject : v;
    }
}
