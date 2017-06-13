package org.lastmilehealth.collect.android.summary;

import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public interface SummaryCollection extends List<Summary> {

    View createView(ViewGroup parent, Collection<InstanceElement> instances);

    void dispose();

}
