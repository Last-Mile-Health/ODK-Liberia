package org.lastmilehealth.collect.android.cases;

import java.util.Collection;
import java.util.Map;

/**
 * This is a container for case instances.
 * The cases are inserted by case UUID.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public interface CaseCollection extends Map<String, Case> {

    Collection<Case> getOpenCases();
}
