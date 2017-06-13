package org.lastmilehealth.collect.android.cases.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 19.05.2017.
 */

public class CaseElementRoot extends CaseElementGroup {

    public CaseElementRoot() {
        super(null);
    }

    @Override
    public View generateView(ViewGroup context,
                             Collection<InstanceElement> secondaryForms) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context.getContext());
            ViewGroup group = (ViewGroup) inflater.inflate(R.layout.case_element_group, context, false);
            for (CaseElement element : caseElements) {
                View elementView = element.generateView(context, secondaryForms);
                if (elementView != null) {
                    group.addView(elementView);
                }
            }
            return group;
        } catch (Exception e) {
            android.util.Log.e("ERROR", "Failed to create case element root", e);
            return null;
        }
    }

}
