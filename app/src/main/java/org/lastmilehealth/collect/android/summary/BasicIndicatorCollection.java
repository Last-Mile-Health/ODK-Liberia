package org.lastmilehealth.collect.android.summary;

import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public class BasicIndicatorCollection extends ArrayList<Indicator> implements IndicatorCollection {

    @Override
    public void addIndicatorViews(ViewGroup parent,
                                  Collection<InstanceElement> instances) {

        for (Indicator indicator : this) {
            View indicatorView = indicator.createView(parent, instances);
            if (indicatorView != null) {
                parent.addView(indicatorView);
            }
        }
    }

    @Override
    public void dispose() {
        for (Indicator indicator : this) {
            indicator.dispose();
        }
        clear();
    }
}
