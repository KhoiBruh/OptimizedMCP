package net.optifine.shaders.config;

public class ShaderMacro {
    private final String name;
    private final String value;

    public ShaderMacro(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSourceLine() {
        return "#define " + name + " " + value;
    }

    public String toString() {
        return getSourceLine();
    }
}
