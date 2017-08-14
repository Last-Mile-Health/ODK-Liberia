package org.lastmilehealth.collect.android.filter.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.filter.FilterTransformer;
import org.lastmilehealth.collect.android.manager.Manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public abstract class FilterPeriod<TYPE> implements Filter<TYPE> {
    private final FilterTransformer<TYPE, Date> typeToDateTransformer;

    public FilterPeriod(FilterTransformer<TYPE, Date> typeToDateTransformer) {
        this.typeToDateTransformer = typeToDateTransformer;
    }

    public static String getFilterVariableName() {
        return Manager.getRetentionManager().getVariableName();
    }

    public static boolean isPeriodFilteringEnabled() {
        return !TextUtils.isEmpty(getFilterVariableName());
    }

    public boolean isEnabled() {
        return FilterPeriod.isPeriodFilteringEnabled();
    }

    @Override
    public List<TYPE> filter(Collection<TYPE> cases) {
        List<TYPE> filtered = new ArrayList<>();
        if (cases != null) {
            for (TYPE instance : cases) {
                Date date = typeToDateTransformer.transform(instance);
                if (date != null) {
                    if (!isFiltered(date)) {
                        filtered.add(instance);
                    }
                }
            }
        }
        return filtered;
    }

    public abstract boolean isFiltered(Date date);
}
