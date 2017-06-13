package org.lastmilehealth.collect.android.cases.impl;

import org.lastmilehealth.collect.android.cases.CaseTextElement;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

/**
 * Created by Anton Donchev on 19.05.2017.
 */

public class CaseElementVariableText extends CaseElementStringText implements CaseTextElement {
    public final String variableName;

    public CaseElementVariableText(String variableName,
                                   String style) {

        super(null, style);
        this.variableName = variableName;
    }

    @Override
    public String getText(InstanceElement instanceElement) {
        if (instanceElement == null) {
            return "<null>";
        }
        return FormsUtils.getVariableValue(variableName, instanceElement);
    }

    @Override
    public String getRawText() {
        return variableName;
    }
}
