package org.lastmilehealth.collect.android.cases;

import android.text.SpannableStringBuilder;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.ArrayList;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public class BasicTextElementsCollection extends ArrayList<CaseTextElement> implements CaseTextElementsCollection {

    @Override
    public CharSequence geneareteText(InstanceElement instance) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (CaseTextElement text : this) {
            CharSequence textFormatted = text.getValue(instance);
            if (textFormatted != null) {
                builder.append(textFormatted);
            }
        }
        return builder;
    }
}
