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

import static org.lastmilehealth.collect.android.utilities.FormsUtils.CASE_STATUS_CLOSED;

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

        if (TextUtils.isEmpty(caseType.getPrimaryFormName())) {
            sendEvent(CaseType.Event.CASE_LIST_FAILED);
        } else {
            try {
                sendEvent(CaseType.Event.CASES_LIST_LOADING);
                instanceCursor = FormsUtils.getInstancesCursor(context, null);
                CaseCollection caseCollection = new CaseCollectionImpl();


                for (boolean hasItem = instanceCursor.moveToFirst(); hasItem; hasItem = instanceCursor.moveToNext()) {
                    try {
                        String instanceFilePath = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                        XmlInstanceParser parser = new XmlInstanceParser(instanceFilePath);
                        InstanceElement root = parser.parse();
                        String formName = root.getAttributes().get(FormsUtils.ATTR_FORM_NAME);
                        if (TextUtils.equals(formName, caseType.getPrimaryFormName())) {
                            String uuid = FormsUtils.getVariableValue(FormsUtils.CASE_UUID, root);
                            String caseStatus = FormsUtils.getVariableValue(FormsUtils.CASE_STATUS, root);
                            if (!TextUtils.isEmpty(uuid) && !TextUtils.equals(caseStatus, FormsUtils.CASE_STATUS_CLOSED)) {
                                CaseImpl instance = new CaseImpl();
                                instance.setPrimaryForm(root);
                                instance.setPrimaryVariableName(caseType.getPrimaryVariable());
                                instance.setUuid(uuid);
                                caseCollection.put(uuid, instance);
                            }
                        }
                    } catch (Exception e) {
                        // Continue to next case or cancel.
                    }
                    if (canceled) {
                        break;
                    }
                }

                if (!canceled) {
                    caseType.getCases().putAll(caseCollection);
                    sendEvent(CaseType.Event.CASES_LIST_LOADED);
                } else {
                    sendEvent(CaseType.Event.CASE_LIST_FAILED);
                }

            } catch (Exception e) {
                sendEvent(CaseType.Event.CASE_LIST_FAILED);
            } finally {
                if (instanceCursor != null) {
                    instanceCursor.close();
                }
            }
        }
    }

    @Override
    public CaseCollection getCaseCollection() {
        return caseCollection;
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
