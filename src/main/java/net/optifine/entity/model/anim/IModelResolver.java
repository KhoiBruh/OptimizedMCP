package net.optifine.entity.model.anim;

import net.optifine.expr.IExpressionResolver;

public interface IModelResolver extends IExpressionResolver {

    ModelVariableFloat getModelVariable(String var1);
}
