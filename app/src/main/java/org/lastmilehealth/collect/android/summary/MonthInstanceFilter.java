package org.lastmilehealth.collect.android.summary;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class MonthInstanceFilter extends PeriodInstanceFilter {

    @Override
    protected long getMinTime() {
        return System.currentTimeMillis() - 2592000000L;
    }
}
