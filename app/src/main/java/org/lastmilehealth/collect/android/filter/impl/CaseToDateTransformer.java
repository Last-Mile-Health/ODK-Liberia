package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.cases.Case;

import java.util.Date;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public class CaseToDateTransformer extends TypeToDateTransformer<Case> {
    @Override
    public Date transform(Case obj) {
        return getInstanceDate(obj.getPrimaryForm());
    }
}
