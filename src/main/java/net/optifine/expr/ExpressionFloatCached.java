package net.optifine.expr;

public class ExpressionFloatCached implements IExpressionFloat, IExpressionCached {
    private final IExpressionFloat expression;
    private boolean cached;
    private float value;

    public ExpressionFloatCached(IExpressionFloat expression) {
        this.expression = expression;
    }

    public float eval() {
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
