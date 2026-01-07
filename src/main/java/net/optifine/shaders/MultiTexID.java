package net.optifine.shaders;

public class MultiTexID {
    public final int base;
    public final int norm;
    public final int spec;

    public MultiTexID(int baseTex, int normTex, int specTex) {
        base = baseTex;
        norm = normTex;
        spec = specTex;
    }
}
