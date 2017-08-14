package org.lastmilehealth.collect.android.retention.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.manager.EventHandlerImpl;
import org.lastmilehealth.collect.android.preferences.AdminPreferencesActivity;
import org.lastmilehealth.collect.android.retention.RetentionTimeDeleteInstancesTask;
import org.lastmilehealth.collect.android.retention.RetentionTimeManager;

/**
 * Created by Anton Donchev on 16.05.2017.
 */

public class RetentionTimeManagerImpl extends EventHandlerImpl implements RetentionTimeManager {
    private final static String PREF_RETENTION_TIME_VARIABLE_NAME = "retention_time_variable_name";
    private final static String PREF_RETENTION_TIME_EXPIRATION = "retention_time_expiration";
    public final static int DEFAULT_RETENTION_EXPIRATION;
    public final static String DEFAULT_RETENTION_VARIABLE_NAME;
    private final static SharedPreferences PREFERENCES;

    static {
        DEFAULT_RETENTION_EXPIRATION = Collect.getInstance().getResources().getInteger(R.integer.retention_time_default_expiration);
        DEFAULT_RETENTION_VARIABLE_NAME = Collect.getInstance().getResources().getString(R.string.retention_time_default_variable_name);
        PREFERENCES = Collect.getInstance().getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, Context.MODE_PRIVATE);
    }

    private RetentionTimeDeleteInstancesTask formDeletionTask;

    @Override
    public void findAndDeleteOldForms() {
        if (!isRetentionTimeEnabled()) {
            onEvent(Event.FORMS_DELETION_DISMISSED);
        } else {
            if (formDeletionTask != null) {
                onEvent(Event.FORMS_DELETION_STARTED);
            } else {
                formDeletionTask = new DefaultRetentionTimeDeleteInstancesTask();
                formDeletionTask.start();
            }
        }
    }

    @Override
    public boolean isRetentionTimeEnabled() {
        String variableName = getVariableName();
        int expirationTime = getExpirationTime();
        return !(TextUtils.isEmpty(variableName) || expirationTime <= 0);
    }

    @Override
    public String getVariableName() {
        return PREFERENCES.getString(PREF_RETENTION_TIME_VARIABLE_NAME, DEFAULT_RETENTION_VARIABLE_NAME);
    }

    @Override
    public int getExpirationTime() {
        return PREFERENCES.getInt(PREF_RETENTION_TIME_EXPIRATION, DEFAULT_RETENTION_EXPIRATION);
    }

    @Override
    public long getExpirationTimeMillies() {
        return getExpirationTime() * 86400000L ;
    }

    @Override
    public String getDetaulfVariableName() {
        return DEFAULT_RETENTION_VARIABLE_NAME;
    }

    @Override
    public int getDefaultExpirationTime() {
        return DEFAULT_RETENTION_EXPIRATION;
    }


    @Override
    public void onEvent(int event) {
        switch (event) {
            case Event.FORMS_DELETION_FINISHED:
                formDeletionTask = null;
                break;
        }
        super.onEvent(event);
    }
}
