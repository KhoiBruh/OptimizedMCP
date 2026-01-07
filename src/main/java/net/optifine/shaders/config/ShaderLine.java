package net.optifine.shaders.config;

import net.minecraft.src.Config;
import net.optifine.util.StrUtils;
import org.joml.Vector4f;

public class ShaderLine {
    private final int type;
    private final String name;
    private final String value;
    private final String line;

    public ShaderLine(int type, String name, String value, String line) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isUniform() {
        return type == 1;
    }

    public boolean isAttribute() {
        return type == 2;
    }

    public boolean isAttribute(String name) {
        return isAttribute() && name.equals(this.name);
    }

    public boolean isProperty() {
        return type == 6;
    }

    public boolean isConstInt() {
        return type == 3;
    }

    public boolean isConstFloat() {
        return type == 4;
    }

    public boolean isConstBool() {
        return type == 5;
    }

    public boolean isExtension() {
        return type == 7;
    }

    public boolean isConstVec4() {
        return type == 8;
    }

    public boolean isProperty(String name) {
        return isProperty() && name.equals(this.name);
    }

    public boolean isProperty(String name, String value) {
        return isProperty(name) && value.equals(this.value);
    }

    public boolean isConstInt(String name) {
        return isConstInt() && name.equals(this.name);
    }

    public boolean isConstIntSuffix(String suffix) {
        return isConstInt() && name.endsWith(suffix);
    }

    public boolean isConstFloat(String name) {
        return isConstFloat() && name.equals(this.name);
    }

    public boolean isConstBool(String name) {
        return isConstBool() && name.equals(this.name);
    }

    public boolean isExtension(String name) {
        return isExtension() && name.equals(this.name);
    }

    public boolean isConstBoolSuffix(String suffix) {
        return isConstBool() && name.endsWith(suffix);
    }

    public boolean isConstBoolSuffix(String suffix, boolean val) {
        return isConstBoolSuffix(suffix) && getValueBool() == val;
    }

    public boolean isConstBool(String name1, String name2) {
        return isConstBool(name1) || isConstBool(name2);
    }

    public boolean isConstBool(String name1, String name2, String name3) {
        return isConstBool(name1) || isConstBool(name2) || isConstBool(name3);
    }

    public boolean isConstBool(String name, boolean val) {
        return isConstBool(name) && getValueBool() == val;
    }

    public boolean isConstBool(String name1, String name2, boolean val) {
        return isConstBool(name1, name2) && getValueBool() == val;
    }

    public boolean isConstBool(String name1, String name2, String name3, boolean val) {
        return isConstBool(name1, name2, name3) && getValueBool() == val;
    }

    public boolean isConstVec4Suffix(String suffix) {
        return isConstVec4() && name.endsWith(suffix);
    }

    public int getValueInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException var2) {
            throw new NumberFormatException("Invalid integer: " + value + ", line: " + line);
        }
    }

    public float getValueFloat() {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException var2) {
            throw new NumberFormatException("Invalid float: " + value + ", line: " + line);
        }
    }

    public Vector4f getValueVec4() {
        if (value == null) {
            return null;
        } else {
            String s = value.trim();
            s = StrUtils.removePrefix(s, "vec4");
            s = StrUtils.trim(s, " ()");
            String[] astring = Config.tokenize(s, ", ");

            if (astring.length != 4) {
                return null;
            } else {
                float[] afloat = new float[4];

                for (int i = 0; i < astring.length; ++i) {
                    String s1 = astring[i];
                    s1 = StrUtils.removeSuffix(s1, new String[]{"F", "f"});
                    float f = Config.parseFloat(s1, Float.MAX_VALUE);

                    if (f == Float.MAX_VALUE) {
                        return null;
                    }

                    afloat[i] = f;
                }

                return new Vector4f(afloat[0], afloat[1], afloat[2], afloat[3]);
            }
        }
    }

    public boolean getValueBool() {
        String s = value.toLowerCase();

        if (!s.equals("true") && !s.equals("false")) {
            throw new RuntimeException("Invalid boolean: " + value + ", line: " + line);
        } else {
            return Boolean.parseBoolean(value);
        }
    }
}
