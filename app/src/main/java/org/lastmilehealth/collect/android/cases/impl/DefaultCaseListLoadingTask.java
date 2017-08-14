package org.lastmilehealth.collect.android.cases.impl;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseListLoadingTask;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.parser.XmlInstanceParser;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anton Donchev on 11.05.2017.
 */

public class DefaultCaseListLoadingTask extends BaseLoadingTask implements CaseListLoadingTask {
    private final CaseType caseType;
    private final Context context;
    private CaseCollection caseCollection;

    public DefaultCaseListLoadingTask(Context context,
                                      CaseType caseType) {
        this.caseType = caseType;
        this.context = context.getApplicationContext();
    }

    @Override
    public void run() {
        Cursor instanceCursor = null;
        Cursor archivesCursor = null;

        if (TextUtils.isEmpty(caseType.getPrimaryFormName())) {
            sendEvent(CaseType.Event.CASE_LIST_FAILED);
        } else {
            try {
                sendEvent(CaseType.Event.CASES_LIST_LOADING);
                instanceCursor = FormsUtils.getInstanceCursor(context, InstanceProviderAPI.InstanceColumns.CONTENT_URI, true);

                CaseCollection caseCollection = new CaseCollectionImpl();
                caseType.resetInstances();

                Map<String, CaseImpl> processedCases = new HashMap<>();

                processCursor(instanceCursor, caseCollection, processedCases, false);

                archivesCursor = FormsUtils.getInstanceCursor(context, InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, true);
                processCursor(archivesCursor, caseCollection, processedCases, true);

                processedCases.clear();

                if (!canceled) {
                    caseType.getCases().putAll(caseCollection);
                    sendEvent(CaseType.Event.CASES_LIST_LOADED);
                    sendEvent(CaseType.Event.CASE_DETAILS_LOADED);
                } else {
                    sendEvent(CaseType.Event.CASE_LIST_FAILED);
                }

            } catch (Exception e) {
                sendEvent(CaseType.Event.CASE_LIST_FAILED);
            } finally {
                if (instanceCursor != null) {
                    instanceCursor.close();
                }
                if (archivesCursor != null) {
                    archivesCursor.close();
                }
            }
        }
    }

    @Override
    public CaseCollection getCaseCollection() {
        return caseCollection;
    }

    private void processCursor(Cursor cursor,
                               CaseCollection caseCollection,
                               Map<String, CaseImpl> processedCases,
                               boolean isArchives) {
        for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
            try {
                String instanceFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                if (isArchives) {
                    instanceFilePath = instanceFilePath.replace(Collect.INSTANCES_PATH, Collect.ARCHIVE_PATH);
                }
                XmlInstanceParser parser = new XmlInstanceParser(instanceFilePath);
                InstanceElement root = parser.parse();
                String formName = root.getAttributes().get(FormsUtils.ATTR_FORM_NAME);
                String uuid = FormsUtils.getVariableValue(FormsUtils.CASE_UUID, root);
                if (!TextUtils.isEmpty(uuid)) {
                    CaseImpl instance = processedCases.get(uuid);
                    if (instance == null) {
                        instance = new CaseImpl();
                        instance.setUuid(uuid);
                        caseCollection.put(uuid, instance);
                        processedCases.put(uuid, instance);
                    }
                    if (TextUtils.equals(formName, caseType.getPrimaryFormName())) {
                        instance.setPrimaryForm(root);
                        instance.setPrimaryVariableName(caseType.getPrimaryVariable());
                    } else if (isSecondaryForm(formName, caseType.getSecondaryFormNames())) {
                        caseType.getSecondaryForms().put(uuid, root);
                        instance.getSecondaryForms().add(root);
                    }
                }
            } catch (Exception e) {
                toString();
                // Continue to next case or cancel.
            }
            if (canceled) {
                break;
            }
        }
    }

    private boolean isSecondaryForm(String formName,
                                    Collection<String> secondaryForms) {
        if (secondaryForms != null && secondaryForms.size() > 0 && !TextUtils.isEmpty(formName)) {
            for (String secondaryFormName : secondaryForms) {
                if (formName.equalsIgnoreCase(secondaryFormName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendEvent(final int eventId) {
        Collect.MAIN_THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                caseType.onEvent(eventId);
            }
        });
    }

}
