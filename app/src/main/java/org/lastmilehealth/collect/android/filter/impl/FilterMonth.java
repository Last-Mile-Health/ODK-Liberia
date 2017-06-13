package org.lastmilehealth.collect.android.filter.impl;

import java.util.Date;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class FilterMonth extends FilterPeriod {
    @Override
    public boolean isFiltered(Date date) {
        long earliest = System.currentTimeMillis() - 2592000000L;

        return date.getTime() < earliest;
    }
}
