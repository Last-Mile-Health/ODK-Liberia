package org.lastmilehealth.collect.android.filter.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.filter.CaseFilter;
import org.lastmilehealth.collect.android.manager.Manager;

/**
 * Created by Anton Donchev on 08.06.2017.
 */

public abstract class BaseFilter implements CaseFilter {

    public static boolean isEnabled() {
        String variableName = Manager.getRetentionManager().getVariableName();
        return !TextUtils.isEmpty(variableName);
    }
}
