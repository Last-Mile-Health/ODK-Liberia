package org.lastmilehealth.collect.android.summary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class SpecialIndicator extends BasicIndicator {


    @Override
    public View createView(ViewGroup parent,
                           Collection<InstanceElement> instances) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(R.layout.case_element_special_hr, parent, false);
    }
}
