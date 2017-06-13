package org.lastmilehealth.collect.android.retention.impl;

import android.content.Context;
import android.database.Cursor;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.impl.BaseLoadingTask;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.parser.XmlInstanceParser;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.retention.RetentionTimeDeleteInstancesTask;
import org.lastmilehealth.collect.android.retention.RetentionTimeManager;
import org.lastmilehealth.collect.android.tasks.SaveToDiskTask;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Finds instances ready for deletion and deletes them.
 * Created by Anton Donchev on 16.05.2017.
 */

public class DefaultRetentionTimeDeleteInstancesTask extends BaseLoadingTask implements RetentionTimeDeleteInstancesTask {

    @Override
    public void run() {
        callEvent(RetentionTimeManager.Event.FORMS_DELETION_STARTED);

        try {
            String variableName = Manager.getRetentionManager().getVariableName();
            long expiration = Manager.getRetentionManager().getExpirationTime();
            final Context context = Collect.getInstance();
            long expirationMillis = 86400000L * expiration;

            Cursor instanceCursor = FormsUtils.getInstancesCursor(Collect.getInstance(), null);
            if (instanceCursor != null) {

                List<File> filesToDelete = new ArrayList<>();

                for (boolean hasItem = instanceCursor.moveToFirst(); hasItem; hasItem = instanceCursor.moveToNext()) {
                    try {
                        String instanceFilePath = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

                        XmlInstanceParser parser = new XmlInstanceParser(instanceFilePath);
                        InstanceElement element = parser.parse();

                        String variableValue = FormsUtils.getVariableValue(variableName, element);

                        Date date = FormsUtils.parseDateString(variableValue);
                        if (date != null) {
                            long endDate = date.getTime() + expirationMillis;
                            if (System.currentTimeMillis() > endDate) {
                                File instanceFile = new File(instanceFilePath);
                                File shadowInstance = SaveToDiskTask.savepointFile(instanceFile);

                                if (instanceFile.exists()) {
                                    filesToDelete.add(instanceFile);                    // Deletes the file
                                    filesToDelete.add(instanceFile.getParentFile());    // Deletes the dir
                                }
                                if (shadowInstance.exists()) {
                                    filesToDelete.add(shadowInstance);                  // Delete the cache file
                                }

                                // Delete the database record.
                                String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " = ?";
                                String[] args = new String[]{instanceFilePath};
                                context.getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, where, args);
                            }
                        }

                    } catch (Exception e) {
                        // Continue to next instance.
                    }
                }

                if (filesToDelete.size() > 0) {
                    for (File file : filesToDelete) {
                        file.delete();
                    }
                }
            }

            success = true;
        } catch (Exception e) {
            success = false;
            error = e;
        }

        callEvent(RetentionTimeManager.Event.FORMS_DELETION_FINISHED);
    }

    private void callEvent(final int event) {
        Collect.MAIN_THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                Manager.getRetentionManager().onEvent(event);
            }
        });
    }
}
