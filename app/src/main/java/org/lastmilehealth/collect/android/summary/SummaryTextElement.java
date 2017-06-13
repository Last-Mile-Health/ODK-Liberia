package org.lastmilehealth.collect.android.summary;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public interface SummaryTextElement {

    CharSequence generateText(Collection<InstanceElement> instances);
}
