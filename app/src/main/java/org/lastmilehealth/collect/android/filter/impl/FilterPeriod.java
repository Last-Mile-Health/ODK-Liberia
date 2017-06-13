package org.lastmilehealth.collect.android.filter.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.filter.CaseFilter;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public abstract class FilterPeriod implements CaseFilter {
    @Override
    public List<Case> filter(Collection<Case> cases) {
        List<Case> filtered = new ArrayList<>();
        if (cases != null) {
            String variableName = Manager.getRetentionManager().getVariableName();
            for (Case instance : cases) {
                String variableValue = FormsUtils.getVariableValue(variableName, instance.getPrimaryForm());
                if (!TextUtils.isEmpty(variableValue)) {
                    Date date = FormsUtils.parseDateString(variableValue);
                    if (date != null) {
                        if (!isFiltered(date)) {
                            filtered.add(instance);
                        }
                    }
                }
            }
        }
        return filtered;
    }

    public abstract boolean isFiltered(Date date);

    public static boolean isEnabled() {
        return !TextUtils.isEmpty(Manager.getRetentionManager().getVariableName());
    }
}
