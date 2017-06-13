package org.lastmilehealth.collect.android.summary.calc;

import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

public class VariableParameter extends BaseFunction {
    private final String variableName;
    private final InstanceElement element;

    public VariableParameter(String variableName,
                             InstanceElement instance) {
        this.variableName = variableName;
        this.element = instance;
    }

    @Override
    public void addParameter(Function parameter) {
        throw new EvaluationException("Cannot add parameters to VariableParameter");
    }

    @Override
    public double evaluate() {
        String variableValue = FormsUtils.getVariableValue(variableName, element);
        return Double.parseDouble(variableValue);
    }

}
