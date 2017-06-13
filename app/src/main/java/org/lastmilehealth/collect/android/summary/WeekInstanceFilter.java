package org.lastmilehealth.collect.android.summary;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class WeekInstanceFilter extends PeriodInstanceFilter {
    @Override
    protected long getMinTime() {
        return System.currentTimeMillis() - 604800000L;
    }
}
