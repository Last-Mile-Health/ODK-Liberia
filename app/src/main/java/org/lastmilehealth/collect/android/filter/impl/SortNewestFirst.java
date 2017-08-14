package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.filter.FilterTransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class SortNewestFirst<TYPE> implements Filter<TYPE> {
    private final FilterTransformer<TYPE, Date> objToDateTransformer;
    private final Comparator<TYPE> comparatorNewestFirst = new Comparator<TYPE>() {
        @Override
        public int compare(TYPE lhs,
                           TYPE rhs) {
            Date lhsDate = objToDateTransformer.transform(lhs);
            Date rhsDate = objToDateTransformer.transform(rhs);

            if (lhsDate == null) {
                if (rhsDate == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if (rhsDate == null) {
                    return -1;
                } else {
                    return rhsDate.after(lhsDate) ? 1 : -1;
                }
            }
        }
    };

    public SortNewestFirst(FilterTransformer<TYPE, Date> objToDateTransformer) {
        this.objToDateTransformer = objToDateTransformer;
    }

    @Override
    public List<TYPE> filter(Collection<TYPE> cases) {
        List<TYPE> sorted = new ArrayList<>(cases);
        Collections.sort(sorted, comparatorNewestFirst);
        return sorted;
    }

    @Override
    public boolean isEnabled() {
        return FilterPeriod.isPeriodFilteringEnabled();
    }

}
