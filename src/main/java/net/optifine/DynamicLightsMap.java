package net.optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicLightsMap {
    private final Map<Integer, DynamicLight> map = new HashMap<>();
    private final List<DynamicLight> list = new ArrayList<>();
    private boolean dirty = false;

    public void put(int id, DynamicLight dynamicLight) {
        DynamicLight dynamiclight = map.put(id, dynamicLight);
        setDirty();
    }

    public DynamicLight get(int id) {
        return map.get(id);
    }

    public int size() {
        return map.size();
    }

    public DynamicLight remove(int id) {
        DynamicLight dynamiclight = map.remove(id);

        if (dynamiclight != null) {
            setDirty();
        }

        return dynamiclight;
    }

    public void clear() {
        map.clear();
        list.clear();
        setDirty();
    }

    private void setDirty() {
        dirty = true;
    }

    public List<DynamicLight> valueList() {
        if (dirty) {
            list.clear();
            list.addAll(map.values());
            dirty = false;
        }

        return list;
    }
}
