package org.lastmilehealth.collect.android.cases.impl;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.javarosa.core.model.instance.TreeElement;
import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseListLoadingTask;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.logic.FormController;
import org.lastmilehealth.collect.android.provider.FormsProviderAPI;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.tasks.FormLoaderTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Donchev on 11.05.2017.
 */

public class DefaultCaseListLoadingTask extends BaseLoadingTask implements CaseListLoadingTask {
    public static final String CASE_UUID = "LMD-VAL-meta_UUID";
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
        List<String> instancePaths = new ArrayList<>();
        List<String> formsPaths = new ArrayList<>();
        Cursor instanceCursor = null;

        if (TextUtils.isEmpty(caseType.getPrimaryFormName())) {
            sendEvent(CaseType.Event.CASES_LIST_FAILED);
        } else {
            try {
                sendEvent(CaseType.Event.CASES_LIST_LOADING);
                String selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ? AND " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " = ?";
                String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED, caseType.getPrimaryFormName()};
//            String sortOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";
                instanceCursor = context.getContentResolver()
                                        .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);

                for (boolean hasItem = instanceCursor.moveToFirst(); hasItem; hasItem = instanceCursor.moveToNext()) {
                    CaseImpl instance = new CaseImpl();

                    String instanceFilePath = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                    String formFilePath = getFormFilePath(instanceCursor, context);
                    if (!TextUtils.isEmpty(formFilePath) && !TextUtils.isEmpty(instanceFilePath)) {
                        instancePaths.add(instanceFilePath);
                        formsPaths.add(formFilePath);
                    }
                    FormLoaderTask task = new FormLoaderTask(instanceFilePath, null, null);
                    task.loadForm(formFilePath);
                    FormController formController = task.getFormController();
                    List<TreeElement> uuidElements = formController.getFormDef().getInstance().getRoot().getChildrenWithName(CASE_UUID);
                    if (uuidElements.size() > 0) {
                        instance.setPrimaryForm(formController.getFormDef());
                        instance.setPrimaryVariableName(caseType.getPrimaryVariable());
                        caseType.getCases().put(uuidElements.get(0).getValue().toString(), instance);
                    }
                }

                sendEvent(CaseType.Event.CASES_LIST_LOADED);
            } catch (Exception e) {
                sendEvent(CaseType.Event.CASES_LIST_FAILED);
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

    private String getFormFilePath(Cursor instanceCursor,
                                   Context context) {
        // get the formId and version for this instance...
        String jrFormId = null;
        String jrVersion = null;
        {
            jrFormId = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
            int idxJrVersion = instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);

            jrVersion = instanceCursor.isNull(idxJrVersion) ? null : instanceCursor.getString(idxJrVersion);
        }

        String[] selectionArgs;
        String selection;

        if (jrVersion == null) {
            selectionArgs = new String[]{jrFormId};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{jrFormId, jrVersion};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + "=?";
        }

        Cursor formCursor = null;
        String formPath = null;
        try {
            formCursor = context.getContentResolver()
                                .query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, selection, selectionArgs, null);
            if (formCursor.getCount() == 1) {
                formCursor.moveToFirst();
                formPath = formCursor.getString(formCursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
            } else {
                formPath = null;
            }
        } finally {
            if (formCursor != null) {
                formCursor.close();
            }
        }
        return formPath;
    }

    private void prepareInstances() {
    }
}
