package org.lastmilehealth.collect.android.summary;

import android.text.SpannableStringBuilder;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class BasicSummaryTextElementCollection extends ArrayList<SummaryTextElement> implements SummaryTextElementCollection {
    @Override
    public CharSequence generateText(Collection<InstanceElement> instances) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (SummaryTextElement textElement : this) {
            CharSequence elementText = textElement.generateText(instances);
            if (elementText != null) {
                builder.append(elementText);
            }
        }
        return builder;
    }
}
