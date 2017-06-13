package org.lastmilehealth.collect.android.summary.calc;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

public interface FunctionParser {

    void setInstances(Collection<InstanceElement> elements);

    FunctionEvaluator parse(String expression);

}
