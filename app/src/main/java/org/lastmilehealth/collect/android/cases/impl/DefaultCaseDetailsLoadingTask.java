package org.lastmilehealth.collect.android.cases.impl;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.Case;
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
        Cursor instanceCursor = null, archivesCursor = null;
        try {
            sendEvent(CaseType.Event.CASE_DETAILS_LOADING);

            caseType.resetInstances();
            final Context context = Collect.getInstance();

            instanceCursor = FormsUtils.getInstanceCursor(context, InstanceProviderAPI.InstanceColumns.CONTENT_URI, true);
            archivesCursor = FormsUtils.getInstanceCursor(context, InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, true);

            if (!processCursor(instanceCursor) || !processCursor(archivesCursor, true)) {
                sendEvent(CaseType.Event.CASE_DETAILS_FAILED);
                caseType.getSecondaryForms().clear();
                return;
            }

            sendEvent(CaseType.Event.CASE_DETAILS_LOADED);
        } catch (Exception e) {
            // Failed to load forms
            sendEvent(CaseType.Event.CASE_DETAILS_FAILED);
        } finally {
            if (instanceCursor != null) {
                instanceCursor.close();
            }
            if (archivesCursor != null) {
                archivesCursor.close();
            }
        }
    }

    private boolean processCursor(Cursor cursor) {
        return processCursor(cursor, false);
    }

    private boolean processCursor(Cursor cursor,
                                  boolean isArchives) {
        for (boolean hasElement = cursor.moveToFirst(); hasElement; hasElement = cursor.moveToNext()) {
            if (canceled) {
                return false;
            }
            try {
                String instancePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                if (isArchives) {
                    instancePath = instancePath.replace(Collect.INSTANCES_PATH, Collect.ARCHIVE_PATH);
                }
                XmlInstanceParser parser;
                parser = new XmlInstanceParser(instancePath);
                InstanceElement element = parser.parse();
                String instanceName;

                if (!TextUtils.isEmpty(instanceName = element.getAttributes().get(FormsUtils.ATTR_FORM_NAME))) {
                    for (String secondaryFormName : caseType.getSecondaryFormNames()) {
                        if (instanceName.equalsIgnoreCase(secondaryFormName)) {
                            String uuid = FormsUtils.getVariableValue(FormsUtils.CASE_UUID, element);
                            if (!TextUtils.isEmpty(uuid)) {
                                caseType.getSecondaryForms().put(uuid, element);
                                Case instance = caseType.findCaseByUUID(uuid);
                                instance.getSecondaryForms().add(element);
                            }

                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Continue to next instance.
            }
        }
        return true;
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
