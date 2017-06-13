package org.lastmilehealth.collect.android.cases;

import org.lastmilehealth.collect.android.parser.InstanceElement;

/**
 * Created by Anton Donchev on 19.05.2017.
 */

public interface CaseTextElement {

    CharSequence getValue(InstanceElement instanceElement);

    String getRawText();
}
