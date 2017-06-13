package org.lastmilehealth.collect.android.summary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public class DisplayIndicator extends BasicIndicator {


    @Override
    public View createView(ViewGroup parent,
                           Collection<InstanceElement> instances) {
        Collection<InstanceElement> filteredInstances = filterInstances(instances);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.view_summary_display, parent, false);
        TextView text = (TextView) view.findViewById(R.id.text);

        CharSequence indicatorText = textElements.generateText(filteredInstances);
        text.setText(indicatorText);

        return view;
    }
}
