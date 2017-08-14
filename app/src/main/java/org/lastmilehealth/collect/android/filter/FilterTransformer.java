package org.lastmilehealth.collect.android.filter;

/**
 * Created by Anton Donchev on 15.06.2017.
 */

public interface FilterTransformer<IN_TYPE, OUT_TYPE> {

    OUT_TYPE transform(IN_TYPE obj);

}
