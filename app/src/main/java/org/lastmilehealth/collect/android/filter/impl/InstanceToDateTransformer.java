package org.lastmilehealth.collect.android.filter.impl;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Date;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public class InstanceToDateTransformer extends TypeToDateTransformer<InstanceElement> {
    @Override
    public Date transform(InstanceElement obj) {
        return getInstanceDate(obj);
    }
}
