package org.lastmilehealth.collect.android.cases;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.List;

/**
 * Created by Anton Donchev on 12.06.2017.
 */

public interface CaseTextElementsCollection extends List<CaseTextElement> {

    CharSequence geneareteText(InstanceElement instance);
}
