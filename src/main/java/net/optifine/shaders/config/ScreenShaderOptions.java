package net.optifine.shaders.config;

public class ScreenShaderOptions {
    private final ShaderOption[] shaderOptions;
    private final int columns;

    public ScreenShaderOptions(String name, ShaderOption[] shaderOptions, int columns) {
        this.shaderOptions = shaderOptions;
        this.columns = columns;
    }

    public ShaderOption[] getShaderOptions() {
        return shaderOptions;
    }

    public int getColumns() {
        return columns;
    }
}
