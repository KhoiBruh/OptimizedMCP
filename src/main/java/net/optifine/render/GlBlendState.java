package net.optifine.render;

public class GlBlendState {
    private boolean enabled;
    private int srcFactor;
    private int dstFactor;
    private int srcFactorAlpha;
    private int dstFactorAlpha;

    public GlBlendState() {
        this(false, 1, 0);
    }

    public GlBlendState(boolean enabled) {
        this(enabled, 1, 0);
    }

    public GlBlendState(boolean enabled, int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
        this.enabled = enabled;
        this.srcFactor = srcFactor;
        this.dstFactor = dstFactor;
        this.srcFactorAlpha = srcFactorAlpha;
        this.dstFactorAlpha = dstFactorAlpha;
    }

    public GlBlendState(boolean enabled, int srcFactor, int dstFactor) {
        this(enabled, srcFactor, dstFactor, srcFactor, dstFactor);
    }

    public void setState(boolean enabled, int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
        this.enabled = enabled;
        this.srcFactor = srcFactor;
        this.dstFactor = dstFactor;
        this.srcFactorAlpha = srcFactorAlpha;
        this.dstFactorAlpha = dstFactorAlpha;
    }

    public void setState(GlBlendState state) {
        enabled = state.enabled;
        srcFactor = state.srcFactor;
        dstFactor = state.dstFactor;
        srcFactorAlpha = state.srcFactorAlpha;
        dstFactorAlpha = state.dstFactorAlpha;
    }

    public void setEnabled() {
        enabled = true;
    }

    public void setDisabled() {
        enabled = false;
    }

    public void setFactors(int srcFactor, int dstFactor) {
        this.srcFactor = srcFactor;
        this.dstFactor = dstFactor;
        srcFactorAlpha = srcFactor;
        dstFactorAlpha = dstFactor;
    }

    public void setFactors(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
        this.srcFactor = srcFactor;
        this.dstFactor = dstFactor;
        this.srcFactorAlpha = srcFactorAlpha;
        this.dstFactorAlpha = dstFactorAlpha;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSrcFactor() {
        return srcFactor;
    }

    public int getDstFactor() {
        return dstFactor;
    }

    public int getSrcFactorAlpha() {
        return srcFactorAlpha;
    }

    public int getDstFactorAlpha() {
        return dstFactorAlpha;
    }

    public boolean isSeparate() {
        return srcFactor != srcFactorAlpha || dstFactor != dstFactorAlpha;
    }

    public String toString() {
        return "enabled: " + enabled + ", src: " + srcFactor + ", dst: " + dstFactor + ", srcAlpha: " + srcFactorAlpha + ", dstAlpha: " + dstFactorAlpha;
    }
}
