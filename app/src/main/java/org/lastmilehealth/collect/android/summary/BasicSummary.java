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

public class BasicSummary implements Summary {
    protected final IndicatorCollection indicators = new BasicIndicatorCollection();
    protected String displayName;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public IndicatorCollection getIndicators() {
        return indicators;
    }

    @Override
    public View createView(ViewGroup parent,
                           Collection<InstanceElement> instances) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View summaryView = inflater.inflate(R.layout.view_summary, parent, false);
        ViewGroup summaryGroup = (ViewGroup) summaryView.findViewById(R.id.summary);
        TextView summaryTitle = (TextView) summaryView.findViewById(R.id.summary_title);

        summaryTitle.setText(displayName);

        if (indicators.size() > 0) {
            indicators.addIndicatorViews(summaryGroup, instances);
        }

        return summaryView;
    }

    @Override
    public void dispose() {
        indicators.dispose();
    }
}
