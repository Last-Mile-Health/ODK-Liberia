package org.lastmilehealth.collect.android.summary;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public interface SummaryTextElementCollection extends List<SummaryTextElement> {

    CharSequence generateText(Collection<InstanceElement> instances);
}
