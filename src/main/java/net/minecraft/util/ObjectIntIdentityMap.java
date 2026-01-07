package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

public class ObjectIntIdentityMap<T> implements IObjectIntIterable<T> {
    private final IdentityHashMap<T, Integer> identityMap = new IdentityHashMap(512);
    private final List<T> objectList = Lists.newArrayList();

    public void put(T key, int value) {
        identityMap.put(key, value);

        while (objectList.size() <= value) {
            objectList.add(null);
        }

        objectList.set(value, key);
    }

    public int get(T key) {
        Integer integer = identityMap.get(key);
        return integer == null ? -1 : integer;
    }

    public final T getByValue(int value) {
        return value >= 0 && value < objectList.size() ? objectList.get(value) : null;
    }

    public Iterator<T> iterator() {
        return Iterators.filter(objectList.iterator(), Predicates.notNull());
    }
}
