package net.optifine.render;

public class GlAlphaState {
    private boolean enabled;
    private int func;
    private float ref;

    public GlAlphaState() {
        this(false, 519, 0.0F);
    }

    public GlAlphaState(boolean enabled) {
        this(enabled, 519, 0.0F);
    }

    public GlAlphaState(boolean enabled, int func, float ref) {
        this.enabled = enabled;
        this.func = func;
        this.ref = ref;
    }

    public void setState(boolean enabled, int func, float ref) {
        this.enabled = enabled;
        this.func = func;
        this.ref = ref;
    }

    public void setState(GlAlphaState state) {
        enabled = state.enabled;
        func = state.func;
        ref = state.ref;
    }

    public void setFuncRef(int func, float ref) {
        this.func = func;
        this.ref = ref;
    }

    public void setEnabled() {
        enabled = true;
    }

    public void setDisabled() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getFunc() {
        return func;
    }

    public float getRef() {
        return ref;
    }

    public String toString() {
        return "enabled: " + enabled + ", func: " + func + ", ref: " + ref;
    }
}
