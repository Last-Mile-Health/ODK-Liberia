package org.lastmilehealth.collect.android.filter.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.filter.FilterTransformer;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.Date;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public abstract class TypeToDateTransformer<TYPE> implements FilterTransformer<TYPE, Date> {

    protected String getInstanceVariableName() {
        return FilterPeriod.getFilterVariableName();
    }

    protected Date getInstanceDate(InstanceElement instance) {
        if (instance == null) {
            return null;
        }

        String variableValue = FormsUtils.getVariableValue(getInstanceVariableName(), instance);
        if (TextUtils.isEmpty(variableValue)) {
            return null;
        }

        Date date = FormsUtils.parseDateString(variableValue);
        return date;
    }


}
