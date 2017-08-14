package org.lastmilehealth.collect.android.cases;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Donchev on 19.05.2017.
 */

public interface SecondaryFormsMap extends Map<String, Collection<InstanceElement>> {

    void putByName(InstanceElement element);

    void putByUUID(InstanceElement element);

    void put(String key, InstanceElement element);

    void dispose();
}
