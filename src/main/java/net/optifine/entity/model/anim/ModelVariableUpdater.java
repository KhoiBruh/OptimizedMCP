package net.optifine.entity.model.anim;

import net.minecraft.src.Config;
import net.optifine.expr.ExpressionParser;
import net.optifine.expr.IExpressionFloat;
import net.optifine.expr.ParseException;

public class ModelVariableUpdater {
    private final String modelVariableName;
    private final String expressionText;
    private ModelVariableFloat modelVariable;
    private IExpressionFloat expression;

    public ModelVariableUpdater(String modelVariableName, String expressionText) {
        this.modelVariableName = modelVariableName;
        this.expressionText = expressionText;
    }

    public boolean initialize(IModelResolver mr) {
        modelVariable = mr.getModelVariable(modelVariableName);

        if (modelVariable == null) {
            Config.warn("Model variable not found: " + modelVariableName);
            return false;
        } else {
            try {
                ExpressionParser expressionparser = new ExpressionParser(mr);
                expression = expressionparser.parseFloat(expressionText);
                return true;
            } catch (ParseException parseexception) {
                Config.warn("Error parsing expression: " + expressionText);
                Config.warn(parseexception.getClass().getName() + ": " + parseexception.getMessage());
                return false;
            }
        }
    }

    public void update() {
        float f = expression.eval();
        modelVariable.setValue(f);
    }
}
