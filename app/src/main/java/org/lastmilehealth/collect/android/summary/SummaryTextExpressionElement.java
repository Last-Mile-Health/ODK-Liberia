package org.lastmilehealth.collect.android.summary;

import org.lastmilehealth.collect.android.cases.impl.CaseElementStringText;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.summary.calc.FunctionEvaluator;
import org.lastmilehealth.collect.android.summary.calc.FunctionParser;

import java.util.Collection;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class SummaryTextExpressionElement extends CaseElementStringText {

    public final String expression;

    public SummaryTextExpressionElement(String expression,
                                        String style) {
        super(null, style);
        this.expression = expression;
    }

    @Override
    public CharSequence generateText(Collection<InstanceElement> instances) {
        FunctionParser parser = Manager.getSummaryManager().newFunctionParser();
        parser.setInstances(instances);


        FunctionEvaluator evaluator;
        try {
            evaluator = parser.parse(expression);
            CharSequence text = evaluator.evaluate();
            return formatText(text);
        } catch (Exception e) {
            return "ERR";
        }
    }

    @Override
    public String getText(InstanceElement instanceElement) {
        return null;
    }

    @Override
    public String getRawText() {
        return expression;
    }
}
