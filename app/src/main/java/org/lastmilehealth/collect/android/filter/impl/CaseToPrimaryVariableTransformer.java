package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.filter.FilterTransformer;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public class CaseToPrimaryVariableTransformer implements FilterTransformer<Case, String> {


    @Override
    public String transform(Case obj) {
        return obj.getPrimaryVariableValue();
    }
}
