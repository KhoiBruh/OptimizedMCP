package net.optifine.expr;

public class ExpressionFloatArrayCached implements IExpressionFloatArray, IExpressionCached {
    private final IExpressionFloatArray expression;
    private boolean cached;
    private float[] value;

    public ExpressionFloatArrayCached(IExpressionFloatArray expression) {
        this.expression = expression;
    }

    public float[] eval() {
        if (!cached) {
            value = expression.eval();
            cached = true;
        }

        return value;
    }

    public void reset() {
        cached = false;
    }

    public ExpressionType getExpressionType() {
        return ExpressionType.FLOAT;
    }

    public String toString() {
        return "cached(" + expression + ")";
    }
}
