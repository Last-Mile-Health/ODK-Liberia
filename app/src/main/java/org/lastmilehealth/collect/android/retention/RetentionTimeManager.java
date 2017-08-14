package org.lastmilehealth.collect.android.retention;

import org.lastmilehealth.collect.android.manager.EventHandler;

/**
 * Created by Anton Donchev on 16.05.2017.
 */

public interface RetentionTimeManager extends EventHandler {

    void findAndDeleteOldForms();

    boolean isRetentionTimeEnabled();

    String getVariableName();

    int getExpirationTime();

    long getExpirationTimeMillies();

    String getDetaulfVariableName();

    int getDefaultExpirationTime();

    interface Event {
        /**
         * Called when a form is deleted.
         */
        int FORM_DELETED = 1001;

        /**
         * Called when the variable name changed in settings.
         */
        int VARIABLE_NAME_CHANGED = 1002;

        /**
         * Called when the retention time is changed in settings.
         */
        int EXPIRATION_TIME_CHANGED = 1003;

        /**
         * Called when the task deleting forms successfully executes.
         */
        int FORMS_DELETION_STARTED = 4;

        /**
         * Called when the task deleting forms finishes.
         */
        int FORMS_DELETION_FINISHED = 5;

        /**
         * Called when the conditions for deletion are not met.
         */
        int FORMS_DELETION_DISMISSED = 6;
    }
}
