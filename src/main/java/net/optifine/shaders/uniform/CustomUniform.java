package net.optifine.shaders.uniform;

import net.optifine.expr.IExpression;
import net.optifine.shaders.SMCLog;

public class CustomUniform {
    private final String name;
    private final UniformType type;
    private final IExpression expression;
    private final ShaderUniformBase shaderUniform;

    public CustomUniform(String name, UniformType type, IExpression expression) {
        this.name = name;
        this.type = type;
        this.expression = expression;
        shaderUniform = type.makeShaderUniform(name);
    }

    public void setProgram(int program) {
        shaderUniform.setProgram(program);
    }

    public void update() {
        if (shaderUniform.isDefined()) {
            try {
                type.updateUniform(expression, shaderUniform);
            } catch (RuntimeException runtimeexception) {
                SMCLog.severe("Error updating custom uniform: " + shaderUniform.getName());
                SMCLog.severe(runtimeexception.getClass().getName() + ": " + runtimeexception.getMessage());
                shaderUniform.disable();
                SMCLog.severe("Custom uniform disabled: " + shaderUniform.getName());
            }
        }
    }

    public void reset() {
        shaderUniform.reset();
    }

    public IExpression getExpression() {
        return expression;
    }

    public String toString() {
        return type.name().toLowerCase() + " " + name;
    }
}
