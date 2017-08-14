package org.lastmilehealth.collect.android.dialog;

import android.content.Context;
import android.support.annotation.NonNull;

import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.filter.impl.InstanceToDateTransformer;
import org.lastmilehealth.collect.android.parser.InstanceElement;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public class SummaryFilterDialog extends FilterDialog<InstanceElement> {
    public SummaryFilterDialog(@NonNull Context context,
                            Filter<InstanceElement> currentFilter) {
        super(context, new InstanceToDateTransformer(), null, currentFilter, null);
        setShowSort(false);
    }
}
