package org.lastmilehealth.collect.android.summary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public class BasicSummaryCollection extends ArrayList<Summary> implements SummaryCollection {
    @Override
    public View createView(ViewGroup parent,
                           Collection<InstanceElement> instances) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View summaryCollectionView = inflater.inflate(R.layout.view_summary_collection, parent, false);

        ViewGroup summaryCollectionGroup = (ViewGroup) summaryCollectionView.findViewById(R.id.summary_collection);

        for (Summary summary : this) {
            View summaryView = summary.createView(summaryCollectionGroup, instances);
            if (summaryView != null) {
                summaryCollectionGroup.addView(summaryView);
            }
        }

        return summaryCollectionView;
    }

    @Override
    public void dispose() {
        for (Summary summary : this) {
            summary.dispose();
        }
        clear();
    }
}
