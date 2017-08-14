package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.filter.FilterTransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class SortAlpha<TYPE> implements Filter<TYPE> {
    private final FilterTransformer<TYPE, String> objToPrimaryVariableValueTranslator;
    private final Comparator<TYPE> alphaComparator = new Comparator<TYPE>() {
        @Override
        public int compare(TYPE lhs,
                           TYPE rhs) {
            String lhPrimaryVariableValue = objToPrimaryVariableValueTranslator.transform(lhs);
            String rhPrimaryVariableValue = objToPrimaryVariableValueTranslator.transform(rhs);
            if (lhPrimaryVariableValue == null) {
                if (rhPrimaryVariableValue == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if (rhPrimaryVariableValue == null) {
                    return -1;
                } else {
                    return lhPrimaryVariableValue.compareToIgnoreCase(rhPrimaryVariableValue);
                }
            }
        }
    };

    public SortAlpha(FilterTransformer<TYPE, String> objToPrimaryVariableValueTranslator) {
        this.objToPrimaryVariableValueTranslator = objToPrimaryVariableValueTranslator;
    }

    @Override
    public List<TYPE> filter(Collection<TYPE> cases) {
        List<TYPE> sorted = new ArrayList<>(cases);
        Collections.sort(sorted, alphaComparator);
        return sorted;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
