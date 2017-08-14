package org.lastmilehealth.collect.android.archive;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Donchev on 22.06.2017.
 */

public class ArchiveCopyTask extends ArchiveTask {


    @Override
    public void run() {
        List<Cursor> unclosedCursors = new ArrayList<>();
        try {
            Collect.createODKDirs();
            File instancesDir = new File(Collect.INSTANCES_PATH);
            File archivesDir = new File(Collect.ARCHIVE_PATH);

            if (!instancesDir.exists() || !instancesDir.isDirectory()) {
                return;
            }

            if (!archivesDir.exists()) {
                archivesDir.mkdirs();
                archivesDir.mkdir();
            }

            // TODO copy only forms that apply.
            // copyDir(instancesDir, archivesDir);


            Cursor instancesCursor = Collect.getInstance()
                                            .getContentResolver()
                                            .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null, null, null);
            unclosedCursors.add(instancesCursor);

            List<ContentValues> contentValuesToInsertInArchives = new ArrayList<>();

            for (boolean hasNext = instancesCursor.moveToNext(); hasNext; hasNext = instancesCursor.moveToNext()) {
                String instanceAppId = instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.APP_ID));
                if (TextUtils.equals(instanceAppId, Collect.getInstance().getAppId())) {
                    ContentValues values = new ContentValues();
                    values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
                    values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI)));
                    values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)));
                    values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID)));
                    values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION)));
                    values.put(InstanceProviderAPI.InstanceColumns.STATUS, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS)));
                    values.put(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE)));
                    values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT)));
                    values.put(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE, instancesCursor.getLong(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE)));
                    values.put(InstanceProviderAPI.InstanceColumns.APP_ID, instancesCursor.getString(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.APP_ID)));
                    values.put(InstanceProviderAPI.InstanceColumns.IS_TRANSFERRED, instancesCursor.getInt(instancesCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.IS_TRANSFERRED)));
                    contentValuesToInsertInArchives.add(values);
                }
            }
            instancesCursor.close();
            unclosedCursors.remove(instancesCursor);

            for (ContentValues values : contentValuesToInsertInArchives) {
                String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " = ?";
                String instanceFilepath = values.getAsString(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
                String[] whereArgs = new String[]{instanceFilepath};
                Cursor cursor = Collect.getInstance()
                                       .getContentResolver()
                                       .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, null, where, whereArgs, null);
                int count = 0;

                if (cursor != null) {
                    unclosedCursors.add(cursor);

                    count = cursor.getCount();

                    cursor.close();
                    unclosedCursors.remove(cursor);


                }

                File instanceFile = new File(instanceFilepath);
                File archiveFile = new File(instanceFilepath.replace(Collect.INSTANCES_PATH, Collect.ARCHIVE_PATH));

                FileUtils.copyFile(instanceFile, archiveFile);

                if (count > 0) {
                    int result = Collect.getInstance()
                                        .getContentResolver()
                                        .update(InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, values, where, whereArgs);
                } else {
                    Uri result = Collect.getInstance()
                                        .getContentResolver()
                                        .insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI_ARCHIVES, values);
                }
            }
        } catch (Exception e) {
            // nothing
        } finally {
            for (Cursor cursor : unclosedCursors) {
                cursor.close();
            }
            callEvent(ArchiveManager.Event.COPIED_INSTANCES_TO_ARCHIVES);
        }
    }

    private void copyDir(File currentDir,
                         File destinationDir) {
        File[] files = currentDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File newDestination = new File(destinationDir, file.getName());
                newDestination.mkdirs();
                copyDir(file, newDestination);
            } else {
                copyFile(file, destinationDir);
            }
        }
    }


    private void copyFile(File source,
                          File destinationDir) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(new File(destinationDir, source.getName()));

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    // nothing
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    // nothing
                }
            }
        }
    }
}
