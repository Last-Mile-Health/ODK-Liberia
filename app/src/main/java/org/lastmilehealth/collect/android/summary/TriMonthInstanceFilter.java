package org.lastmilehealth.collect.android.summary;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class TriMonthInstanceFilter extends PeriodInstanceFilter {
    @Override
    protected long getMinTime() {
        return System.currentTimeMillis() - 7776000000L;
    }
}
