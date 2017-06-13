package org.lastmilehealth.collect.android.summary;

import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public interface Indicator {
    String getType();

    String getFormName();

    SummaryTextElementCollection getTextElements();

    View createView(ViewGroup parent,
                    Collection<InstanceElement> instances);

    void dispose();

}
