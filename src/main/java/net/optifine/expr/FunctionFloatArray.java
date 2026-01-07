package net.optifine.expr;

public class FunctionFloatArray implements IExpressionFloatArray {
    private final FunctionType type;
    private final IExpression[] arguments;

    public FunctionFloatArray(FunctionType type, IExpression[] arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    public float[] eval() {
        return type.evalFloatArray(arguments);
    }

    public ExpressionType getExpressionType() {
        return ExpressionType.FLOAT_ARRAY;
    }

    public String toString() {
        return type + "()";
    }
}
