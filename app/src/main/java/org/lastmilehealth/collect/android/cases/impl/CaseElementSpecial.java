package org.lastmilehealth.collect.android.cases.impl;

import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by eXirrah on 22-May-17.
 */

public class CaseElementSpecial extends BasicCaseElement {
    public static final String HORIZONTAL_RULER = "HR";

    @Override
    public View generateView(ViewGroup context,
                             Collection<InstanceElement> secondaryForms) {
        String text = "";
        if (texts.size() > 0) {
            text = texts.get(0).getRawText();
        }
        View view = null;
        if (HORIZONTAL_RULER.equalsIgnoreCase(text)) {
            view = inflate(context, R.layout.case_element_special_hr);
        }
        return view;
    }
}
