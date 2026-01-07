package net.optifine.shaders.config;

import java.util.*;

public class ShaderProfile {
    private final String name;
    private final Map<String, String> mapOptionValues = new LinkedHashMap<>();
    private final Set<String> disabledPrograms = new LinkedHashSet<>();

    public ShaderProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addOptionValue(String option, String value) {
        mapOptionValues.put(option, value);
    }

    public void addOptionValues(ShaderProfile prof) {
        if (prof != null) {
            mapOptionValues.putAll(prof.mapOptionValues);
        }
    }

    public String[] getOptions() {
        Set<String> set = mapOptionValues.keySet();
        return set.toArray(new String[0]);
    }

    public String getValue(String key) {
        return mapOptionValues.get(key);
    }

    public void addDisabledProgram(String program) {
        disabledPrograms.add(program);
    }

    public void removeDisabledProgram(String program) {
        disabledPrograms.remove(program);
    }

    public Collection<String> getDisabledPrograms() {
        return new LinkedHashSet<>(disabledPrograms);
    }

    public void addDisabledPrograms(Collection<String> programs) {
        disabledPrograms.addAll(programs);
    }

    public boolean isProgramDisabled(String program) {
        return disabledPrograms.contains(program);
    }
}
