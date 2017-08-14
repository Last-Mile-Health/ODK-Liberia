package org.lastmilehealth.collect.android.dialog;

import android.content.Context;
import android.support.annotation.NonNull;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.filter.impl.CaseToDateTransformer;
import org.lastmilehealth.collect.android.filter.impl.CaseToPrimaryVariableTransformer;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public class CaseFilterDialog extends FilterDialog<Case> {
    public CaseFilterDialog(@NonNull Context context,
                            Filter<Case> currentFilter,
                            Filter<Case> currentSort) {
        super(context, new CaseToDateTransformer(), new CaseToPrimaryVariableTransformer(), currentFilter, currentSort);
        setShowSort(true);
    }
}
