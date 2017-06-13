package org.lastmilehealth.collect.android.summary;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.annotation.Nonnull;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public abstract class PeriodInstanceFilter implements InstanceFilter {
    @Override
    @Nonnull
    public Collection<InstanceElement> filter(Collection<InstanceElement> originalInstances) {
        Collection<InstanceElement> filteredElements = new ArrayList<>();
        if (originalInstances != null && originalInstances.size() > 0) {
            String variableName = Manager.getRetentionManager().getVariableName();
            if (TextUtils.isEmpty(variableName)) {
                return filteredElements;
            }
            long minTime = getMinTime();
            for (InstanceElement element : originalInstances) {
                String dateText = FormsUtils.getVariableValue(variableName, element);
                if (!TextUtils.isEmpty(dateText)) {
                    Date date = FormsUtils.parseDateString(dateText);
                    if (date != null && date.getTime() >= minTime) {
                        filteredElements.add(element);
                    }
                }
            }
        }
        return filteredElements;
    }

    protected abstract long getMinTime();

    public static boolean isEnabled() {
        return !TextUtils.isEmpty(Manager.getRetentionManager().getVariableName());
    }
}
