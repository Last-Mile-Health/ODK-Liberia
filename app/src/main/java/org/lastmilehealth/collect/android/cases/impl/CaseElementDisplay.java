package org.lastmilehealth.collect.android.cases.impl;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.Collection;

/**
 * Created by eXirrah on 22-May-17.
 */

public class CaseElementDisplay extends BasicCaseElement {

    @Override
    public View generateView(ViewGroup context,
                             Collection<InstanceElement> secondaryForms) {
        View view = inflate(context, R.layout.case_element_display);
        TextView display = (TextView) view.findViewById(R.id.display);
        if (secondaryForms != null && secondaryForms.size() > 0) {
            InstanceElement element = null;
            if (!TextUtils.isEmpty(formName)) {
                for (InstanceElement pickedElement : secondaryForms) {
                    String pickedElementName = pickedElement.getAttributes().get(FormsUtils.ATTR_FORM_NAME);
                    if (formName.equalsIgnoreCase(pickedElementName)) {
                        element = pickedElement;
                        break;
                    }
                }
            } else if (secondaryForms.size() == 1) {
                element = secondaryForms.iterator().next();
            }
            display.setText(generateText(element));
        }
        return display;
    }
}
