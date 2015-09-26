package org.lastmilehealth.collect.android.activities;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.lastmilehealth.collect.android.provider.FormsProviderAPI;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.utilities.Constants;


class MainMenuIncomingHandler extends Handler {
    private final MainMenuActivity activity;

    MainMenuIncomingHandler(MainMenuActivity target) {
        activity = target;
    }

    @Override
    public void handleMessage(Message msg) {
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);

        switch (msg.what) {
            case Constants.MESSAGE_DATA_SUCCESS_SENT:
                NotificationCompat.Builder notificationBuilder1 =
                        new NotificationCompat.Builder(activity)
                                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                                .setContentTitle("Liberia ODK")
                                .setContentText("Upload successful");

                notificationManager.notify(0, notificationBuilder1.build());

                Intent i = new Intent(ProgressActivity.ACTION_CONNECTION_SUCCESS);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(i);

                activity.getContentResolver().notifyChange(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null);
                activity.invalidateOptionsMenu();
                break;
            case Constants.MESSAGE_CONNECTION_LIMIT:
                NotificationCompat.Builder notificationBuilder3 =
                        new NotificationCompat.Builder(activity)
                                .setSmallIcon(android.R.drawable.stat_notify_error)
                                .setContentTitle("Liberia ODK")
                                .setContentText("Connection failed. Please try again");

                notificationManager.notify(0, notificationBuilder3.build());

                Intent i2 = new Intent(ProgressActivity.ACTION_CONNECTION_TIMEOUT);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(i2);
                break;
            case Constants.MESSAGE_CONNECTED:
                Intent i3 = new Intent(ProgressActivity.ACTION_CONNECTED);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(i3);
                break;
            case Constants.MESSAGE_DATA_RECEIVED:
                NotificationCompat.Builder notificationBuilder2 =
                        new NotificationCompat.Builder(activity)
                                .setContentTitle("Liberia ODK")
                                .setContentText("Data processing in progress")
                                .setSmallIcon(android.R.drawable.ic_popup_sync)
                                .setProgress(0, 0, true)
                                .setOngoing(true);

                notificationManager.notify(1, notificationBuilder2.build());

                BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                if (bt.isEnabled())
                    bt.disable();
                break;
            case Constants.MESSAGE_DATA_PROCESSED:
                notificationManager.cancelAll();

                activity.getContentResolver().notifyChange(FormsProviderAPI.FormsColumns.CONTENT_URI, null);
                activity.getContentResolver().notifyChange(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null);
                activity.invalidateOptionsMenu();

                //propose to start accept new data
                /**Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, activity.DISCOVERABLE_DURATION);
                activity.startActivityForResult(discoverableIntent, activity.REQUEST_MAKE_DISCOVERABLE);*/
                break;
            case Constants.MESSAGE_DATA_PROCESS_ERROR:
                //should not happen, means received data is invalid
                notificationManager.cancelAll();
                break;
            case Constants.MESSAGE_DATA_NOT_SENDED:
                //happens if bt connection is lost during data transfer
                Intent intentNotSended = new Intent(ProgressActivity.ACTION_DATA_INVALID);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intentNotSended);
                break;
        }
    }
}