package org.lastmilehealth.collect.android.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;


public class ProgressActivity extends Activity {
    private static final String TAG = "~";
    private TextView statusTextView;
    private String statusText;
    //private String deviceName;
    public static final String ACTION_CONNECTION_TIMEOUT = "ACTION_CONNECTION_TIMEOUT";
    public static final String ACTION_CONNECTION_SUCCESS = "ACTION_CONNECTION_SUCCESS";
    public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
    public static final String ACTION_DATA_INVALID = "ACTION_DATA_INVALID";
    public static final String DEVICE_NAME_PARAM = "DEVICE_NAME_PARAM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_progress);

        // Register for broadcasts
        IntentFilter filter = new IntentFilter(ACTION_CONNECTION_TIMEOUT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        IntentFilter filter2 = new IntentFilter(ACTION_CONNECTION_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter2);
        IntentFilter filter3 = new IntentFilter(ACTION_DATA_INVALID);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter3);
        IntentFilter filter4 = new IntentFilter(ACTION_CONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter4);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressActivity.this.finish();
            }
        });

        statusTextView = (TextView)findViewById(R.id.status_text);
        if(savedInstanceState!=null) {
            //deviceName = savedInstanceState.getString(DEVICE_NAME_PARAM);
            statusText =  savedInstanceState.getString("status");
        } else {
            //deviceName = getIntent().getExtras().getString(DEVICE_NAME_PARAM);
            statusText = getString(R.string.connection_started);
        }

        showStatus();

        com.github.snowdream.android.util.Log.d(ProgressActivity.class.getName(), "[onCreate] statusText : " + statusTextView.getText());
    }

    private void showStatus() {
        if(statusText.equals(getString(R.string.connection_started)) || statusText.equals(getString(R.string.connection_connected))) {
            setProgressBarIndeterminateVisibility(true);
        } else {
            setProgressBarIndeterminateVisibility(false);
        }

        if(statusText.equals(getString(R.string.connection_success))) {
            ((Button)findViewById(R.id.close)).setText(getString(R.string.data_transfer_ok));
        }

        statusTextView.setText(statusText);

        com.github.snowdream.android.util.Log.d(ProgressActivity.class.getName(), "[showStatus] statusText : " + statusText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("status", statusText);
        //outState.putString(DEVICE_NAME_PARAM, deviceName);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            com.github.snowdream.android.util.Log.d(ProgressActivity.class.getName(), "[BroadcastReceiver onReceive] action : " + action);

            if (ACTION_CONNECTION_TIMEOUT.equals(action)) {
                statusText = getString(R.string.connection_timeout);
                showStatus();
            } else if(ACTION_CONNECTED.equals(action)) {
                statusText = getString(R.string.connection_connected);
                showStatus();
            } else if (ACTION_CONNECTION_SUCCESS.equals(action)) {
                statusText = getString(R.string.connection_success);
                showStatus();
            } else if (ACTION_DATA_INVALID.equals(action)) {
                statusText = getString(R.string.data_transfer_failed);
                showStatus();
            }
        }
    };

}
