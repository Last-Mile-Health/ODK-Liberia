package org.lastmilehealth.collect.android.archive;

import android.database.Cursor;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;

/**
 * Created by Anton Donchev on 22.06.2017.
 */

public class ArchiveLoaderTask extends ArchiveTask {
    private int count = 0;

    @Override
    public void run() {
        Cursor cursor = null;
        try {
            cursor = Collect.getInstance()
                            .getContentResolver()
                            .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, null, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            count = 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            callEvent(ArchiveManager.Event.LOADED_ARCHIVES_COUNT);
        }
    }

    public int getCount() {
        return count;
    }

}
