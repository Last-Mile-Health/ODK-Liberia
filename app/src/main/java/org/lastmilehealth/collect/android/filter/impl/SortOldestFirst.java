package org.lastmilehealth.collect.android.filter.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.filter.CaseFilter;
import org.lastmilehealth.collect.android.manager.Manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class SortOldestFirst implements CaseFilter {
    private final Comparator<Case> comparatorOldestFirst = new Comparator<Case>() {
        @Override
        public int compare(Case lhs,
                           Case rhs) {
            Date lhsDate = lhs.getLastModifiedDate();
            Date rhsDate = rhs.getLastModifiedDate();
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
                    return rhsDate.after(lhsDate) ? -1 : 1;
                }
            }
        }
    };

    @Override
    public List<Case> filter(Collection<Case> cases) {
        List<Case> sorted = new ArrayList<>(cases);
        Collections.sort(sorted, comparatorOldestFirst);
        return sorted;
    }

    public static boolean isEnabled() {
        return !TextUtils.isEmpty(Manager.getRetentionManager().getVariableName());
    }

}
