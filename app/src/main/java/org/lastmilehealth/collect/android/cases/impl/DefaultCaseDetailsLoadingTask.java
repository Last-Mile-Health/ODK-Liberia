package org.lastmilehealth.collect.android.cases.impl;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.parser.XmlInstanceParser;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

/**
 * Loads secondary forms in case type.
 * <p>
 * Created by eXirrah on 26-May-17.
 */

public class DefaultCaseDetailsLoadingTask extends BaseLoadingTask {
    private final CaseType caseType;

    public DefaultCaseDetailsLoadingTask(CaseType caseType) {
        this.caseType = caseType;
    }

    @Override
    public void run() {
        try {
            sendEvent(CaseType.Event.CASE_DETAILS_LOADING);
            XmlInstanceParser parser;
            caseType.getSecondaryForms().clear();
            final Context context = Collect.getInstance();
            Cursor cursor = FormsUtils.getInstancesCursor(context, null);
            for (boolean hasElement = cursor.moveToFirst(); hasElement; hasElement = cursor.moveToNext()) {
                if (canceled) {
                    sendEvent(CaseType.Event.CASE_DETAILS_FAILED);
                    caseType.getSecondaryForms().clear();
                    return;
                }
                try {
                    String instancePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

                    parser = new XmlInstanceParser(instancePath);
                    InstanceElement element = parser.parse();
                    String instanceName;
                    if (!TextUtils.isEmpty(instanceName = element.getAttributes().get(FormsUtils.ATTR_FORM_NAME))) {
                        for (String secondaryFormName : caseType.getSecondaryFormNames()) {
                            if (instanceName.equalsIgnoreCase(secondaryFormName)) {
                                caseType.getSecondaryForms().putByUUID(element);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next instance.
                }
            }
            sendEvent(CaseType.Event.CASE_DETAILS_LOADED);
        } catch (Exception e) {
            // Failed to load forms
            sendEvent(CaseType.Event.CASE_DETAILS_FAILED);
        }
    }

    private void sendEvent(final int event) {
        Collect.MAIN_THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                caseType.onEvent(event);
            }
        });
    }

}
