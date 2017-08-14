package org.lastmilehealth.collect.android.cases.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.cases.SecondaryFormsMap;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by eXirrah on 26-May-17.
 */

class DefaultSecondaryFormMaps extends HashMap<String, Collection<InstanceElement>> implements SecondaryFormsMap {

    @Override
    public void putByName(InstanceElement element) {
        String name = element.getAttributes().get(FormsUtils.ATTR_FORM_NAME);
        if (!TextUtils.isEmpty(name)) {
            put(name, element);
        }
    }

    @Override
    public void putByUUID(InstanceElement element) {
        String uuid = FormsUtils.getVariableValue(FormsUtils.CASE_UUID, element);
        if (!TextUtils.isEmpty(uuid)) {
            put(uuid, element);
        }
    }

    @Override
    public void put(String key,
                    InstanceElement element) {
        Collection<InstanceElement> elements = get(key);
        if (elements == null) {
            elements = new HashSet<>();
            put(key, elements);
        }
        elements.add(element);
    }

    @Override
    public void dispose() {
        for (Collection<InstanceElement> elementCollection : values()) {
            for (InstanceElement element : elementCollection) {
                element.dispose();
            }
            elementCollection.clear();
        }
        clear();
    }
}
