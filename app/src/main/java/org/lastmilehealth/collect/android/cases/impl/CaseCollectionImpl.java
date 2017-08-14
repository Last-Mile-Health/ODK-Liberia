package org.lastmilehealth.collect.android.cases.impl;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Anton Donchev on 05.05.2017.
 */

public class CaseCollectionImpl extends HashMap<String, Case> implements CaseCollection {
    @Override
    public Collection<Case> getOpenCases() {
        Collection<Case> openCases = new ArrayList<>();
        if (size() > 0) {
            for (Case instnace :values()){
                if (!instnace.isClosed()) {
                    openCases.add(instnace);
                }
            }
        }
        return openCases;
    }
}
