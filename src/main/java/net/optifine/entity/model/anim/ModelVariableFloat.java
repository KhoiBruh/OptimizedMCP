package net.optifine.entity.model.anim;

import net.minecraft.client.model.ModelRenderer;
import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpressionFloat;

public class ModelVariableFloat implements IExpressionFloat {
    private final String name;
    private final ModelRenderer modelRenderer;
    private final ModelVariableType enumModelVariable;

    public ModelVariableFloat(String name, ModelRenderer modelRenderer, ModelVariableType enumModelVariable) {
        this.name = name;
        this.modelRenderer = modelRenderer;
        this.enumModelVariable = enumModelVariable;
    }

    public ExpressionType getExpressionType() {
        return ExpressionType.FLOAT;
    }

    public float eval() {
        return getValue();
    }

    public float getValue() {
        return enumModelVariable.getFloat(modelRenderer);
    }

    public void setValue(float value) {
        enumModelVariable.setFloat(modelRenderer, value);
    }

    public String toString() {
        return name;
    }
}
