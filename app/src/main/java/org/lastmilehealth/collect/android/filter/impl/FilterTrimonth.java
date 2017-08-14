package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.filter.FilterTransformer;

import java.util.Date;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class FilterTrimonth<TYPE> extends FilterPeriod<TYPE> {


    public FilterTrimonth(FilterTransformer<TYPE, Date> typeToDateTransformer) {
        super(typeToDateTransformer);
    }

    @Override
    public boolean isFiltered(Date date) {
        long earliest = System.currentTimeMillis() - 7776000000L;

        return date.getTime() < earliest;
    }
}
