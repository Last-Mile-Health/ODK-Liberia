package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.filter.FilterTransformer;

import java.util.Date;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class FilterWeek<TYPE> extends FilterPeriod<TYPE> {


    public FilterWeek(FilterTransformer<TYPE, Date> typeToDateTransformer) {
        super(typeToDateTransformer);
    }

    @Override
    public boolean isFiltered(Date date) {
        long earlies = System.currentTimeMillis() - 604800000;
        return date.getTime() < earlies;
    }
}
