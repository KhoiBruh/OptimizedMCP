package net.optifine.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParametersVariable implements IParameters {
    private static final ExpressionType[] EMPTY = new ExpressionType[0];
    private final ExpressionType[] first;
    private final ExpressionType[] repeat;
    private final ExpressionType[] last;
    private final int maxCount;

    public ParametersVariable() {
        this(null, null, null);
    }

    public ParametersVariable(ExpressionType[] first, ExpressionType[] repeat, ExpressionType[] last) {
        this(first, repeat, last, Integer.MAX_VALUE);
    }

    public ParametersVariable(ExpressionType[] first, ExpressionType[] repeat, ExpressionType[] last, int maxCount) {
        this.first = normalize(first);
        this.repeat = normalize(repeat);
        this.last = normalize(last);
        this.maxCount = maxCount;
    }

    private static ExpressionType[] normalize(ExpressionType[] exprs) {
        return exprs == null ? EMPTY : exprs;
    }

    public ExpressionType[] getParameterTypes(IExpression[] arguments) {
        int i = first.length + last.length;
        int j = arguments.length - i;
        int k = 0;

        for (int l = 0; l + repeat.length <= j && i + l + repeat.length <= maxCount; l += repeat.length) {
            ++k;
        }

        List<ExpressionType> list = new ArrayList<>(Arrays.asList(first));

        for (int i1 = 0; i1 < k; ++i1) {
            list.addAll(Arrays.asList(repeat));
        }

        list.addAll(Arrays.asList(last));
        return list.toArray(new ExpressionType[0]);
    }

    public ParametersVariable first(ExpressionType... first) {
        return new ParametersVariable(first, repeat, last);
    }

    public ParametersVariable repeat(ExpressionType... repeat) {
        return new ParametersVariable(first, repeat, last);
    }

    public ParametersVariable last(ExpressionType... last) {
        return new ParametersVariable(first, repeat, last);
    }

    public ParametersVariable maxCount(int maxCount) {
        return new ParametersVariable(first, repeat, last, maxCount);
    }
}
