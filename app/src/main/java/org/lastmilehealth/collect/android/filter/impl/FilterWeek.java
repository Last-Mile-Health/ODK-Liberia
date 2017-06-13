package org.lastmilehealth.collect.android.filter.impl;

import java.util.Date;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class FilterWeek extends FilterPeriod {

    @Override
    public boolean isFiltered(Date date) {
        long earlies = System.currentTimeMillis() - 604800000;
        return date.getTime() < earlies;
    }
}
