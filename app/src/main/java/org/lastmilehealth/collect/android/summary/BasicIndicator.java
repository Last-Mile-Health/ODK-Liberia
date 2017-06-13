package org.lastmilehealth.collect.android.summary;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public abstract class BasicIndicator implements Indicator {
    protected final SummaryTextElementCollection textElements = new BasicSummaryTextElementCollection();
    protected String type;
    protected String formName;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    @Override
    public SummaryTextElementCollection getTextElements() {
        return textElements;
    }

    @Override
    public void dispose() {
        textElements.clear();
    }

    protected Collection<InstanceElement> filterInstances(Collection<InstanceElement> originalElements) {
        Collection<InstanceElement> elements = new ArrayList<>();
        if (!TextUtils.isEmpty(formName) && originalElements != null) {
            for (InstanceElement element : originalElements) {
                if (formName.equalsIgnoreCase(FormsUtils.getFormName(element))) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }
}
