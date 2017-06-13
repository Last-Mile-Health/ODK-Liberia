package org.lastmilehealth.collect.android.filter;

import org.lastmilehealth.collect.android.cases.Case;

import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public interface CaseFilter {

    List<Case> filter(Collection<Case> cases);
}
