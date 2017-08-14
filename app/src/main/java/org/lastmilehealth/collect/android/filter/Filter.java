package org.lastmilehealth.collect.android.filter;

import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public interface Filter<TYPE> {

    List<TYPE> filter(Collection<TYPE> cases);

    boolean isEnabled();
}