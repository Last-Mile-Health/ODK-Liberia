package org.lastmilehealth.collect.android.summary;

import android.view.ViewGroup;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public interface IndicatorCollection extends List<Indicator>  {

    void addIndicatorViews(ViewGroup parent, Collection<InstanceElement> instances);

    void dispose();
}
