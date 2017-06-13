package org.lastmilehealth.collect.android.summary;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public interface InstanceFilter {

    Collection<InstanceElement> filter(Collection<InstanceElement> originalInstances);
}
