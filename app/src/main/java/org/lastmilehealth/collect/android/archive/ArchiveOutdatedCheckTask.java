package org.lastmilehealth.collect.android.archive;

import android.database.Cursor;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;

import java.io.File;

/**
 * Created by Anton Donchev on 22.06.2017.
 */

public class ArchiveOutdatedCheckTask extends ArchiveTask {
    public static final long DELETE_CYCLE = 7776000000L;

    @Override
    public void run() {
        try {
            long deleteMarkerDate = System.currentTimeMillis() - Manager.getRetentionManager().getExpirationTimeMillies();
            Cursor cursor = Collect.getInstance()
                                   .getContentResolver()
                                   .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, null, null, null, null);
            if (cursor != null) {
                for (boolean hasNext = cursor.moveToNext(); hasNext; hasNext = cursor.moveToNext()) {
                    Long dateMillies = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
                    if (dateMillies < deleteMarkerDate) {
                        String instanceFilename = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                        String archiveFilename = instanceFilename.replace(Collect.INSTANCES_PATH, Collect.ARCHIVE_PATH);
                        File archivedFile = new File(archiveFilename);
                        File folder = archivedFile.getParentFile();
                        if (archivedFile.exists()) {
                            archivedFile.delete();
                        }
                        if (folder.exists()) {
                            folder.delete();
                        }
                        String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " = ?";
                        String args[] = {instanceFilename};
                        Collect.getInstance()
                               .getContentResolver()
                               .delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, where, args);
                    }
                }
            }
        } catch (Exception e) {
            // Nothing just try next time.
        } finally {
            callEvent(ArchiveManager.Event.DELETED_OLD_ARCHIVES);
        }
    }
}
