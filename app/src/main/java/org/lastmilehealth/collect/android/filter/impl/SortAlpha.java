package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.filter.CaseFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class SortAlpha implements CaseFilter {
    private final Comparator<Case> alphaComparator = new Comparator<Case>() {
        @Override
        public int compare(Case lhs,
                           Case rhs) {
            return lhs.getPrimaryVariableValue().compareToIgnoreCase(rhs.getPrimaryVariableValue());
        }
    };

    public static boolean isEnabled() {
       return true;
    }

    @Override
    public List<Case> filter(Collection<Case> cases) {
        List<Case> sorted = new ArrayList<>(cases);
        Collections.sort(sorted, alphaComparator);
        return sorted;
    }
}
