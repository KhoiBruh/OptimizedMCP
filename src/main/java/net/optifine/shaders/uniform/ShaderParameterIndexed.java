package net.optifine.shaders.uniform;

import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpressionFloat;

public class ShaderParameterIndexed implements IExpressionFloat {
    private final ShaderParameterFloat type;
    private final int index1;
    private final int index2;

    public ShaderParameterIndexed(ShaderParameterFloat type) {
        this(type, 0, 0);
    }

    public ShaderParameterIndexed(ShaderParameterFloat type, int index1) {
        this(type, index1, 0);
    }

    public ShaderParameterIndexed(ShaderParameterFloat type, int index1, int index2) {
        this.type = type;
        this.index1 = index1;
        this.index2 = index2;
    }

    public float eval() {
        return type.eval(index1, index2);
    }

    public ExpressionType getExpressionType() {
        return ExpressionType.FLOAT;
    }

    public String toString() {
        return type.getIndexNames1() == null ? "" + type : (type.getIndexNames2() == null ? type + "." + type.getIndexNames1()[index1] : type + "." + type.getIndexNames1()[index1] + "." + type.getIndexNames2()[index2]);
    }
}
