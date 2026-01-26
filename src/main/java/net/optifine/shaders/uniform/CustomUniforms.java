package net.optifine.shaders.uniform;

import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionCached;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomUniforms {
    private final CustomUniform[] uniforms;
    private final IExpressionCached[] expressionsCached;

    public CustomUniforms(CustomUniform[] uniforms, Map<String, IExpression> mapExpressions) {
        this.uniforms = uniforms;
        List<IExpressionCached> list = new ArrayList<>();

        for (IExpression iexpression : mapExpressions.values()) {

            if (iexpression instanceof IExpressionCached iexpressioncached) {
                list.add(iexpressioncached);
            }
        }

        expressionsCached = list.toArray(new IExpressionCached[0]);
    }

    public void setProgram(int program) {
        for (CustomUniform customuniform : uniforms) {
            customuniform.setProgram(program);
        }
    }

    public void update() {
        resetCache();

        for (CustomUniform customuniform : uniforms) {
            customuniform.update();
        }
    }

    private void resetCache() {
        for (IExpressionCached iexpressioncached : expressionsCached) {
            iexpressioncached.reset();
        }
    }

    public void reset() {
        for (CustomUniform customuniform : uniforms) {
            customuniform.reset();
        }
    }
}
