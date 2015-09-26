/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.lastmilehealth.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.parser.TinyDB;
import org.lastmilehealth.collect.android.parser.XMLParser;
import org.lastmilehealth.collect.android.preferences.AdminPreferencesActivity;
import org.lastmilehealth.collect.android.preferences.PreferencesActivity;
import org.lastmilehealth.collect.android.provider.FormsProviderAPI;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.lastmilehealth.collect.android.tasks.BluetoothService;
import org.lastmilehealth.collect.android.utilities.CompatibilityUtils;
import org.lastmilehealth.collect.android.utilities.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "~";

    public static final int DISCOVERABLE_DURATION = 300;

    private static final int PASSWORD_DIALOG = 1;
    public static final int REQUEST_MAKE_DISCOVERABLE = 2;
    private static final int REQUEST_ENABLE_BT_FORMS = 3;
    private static final int REQUEST_ENABLE_BT_INSTANCES = 4;
    private static final int REQUEST_CONNECT_DEVICE_FORMS = 5;
    private static final int REQUEST_CONNECT_DEVICE_INSTANCES = 6;

    // menu options
    private static final int MENU_PREFERENCES = Menu.FIRST;
    private static final int MENU_ADMIN = Menu.FIRST + 1;
    private static final int MENU_APP_UPDATE = Menu.FIRST + 2;
    private static final int SEND_FORMS = Menu.FIRST + 3;
    private static final int SEND_INSTANCES = Menu.FIRST + 4;

    // local Bluetooth adapter and service
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;

    // buttons
    private Button mEnterDataButton;
    private Button mManageFilesButton;
    private Button mReviewDataButton;


    private AlertDialog mAlertDialog;
    private SharedPreferences mAdminPreferences;
    private final MainMenuIncomingHandler mHandler = new MainMenuIncomingHandler(this);

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private final int LOADER_FORMS_ID = 1;
    private final int LOADER_INSTANCES_ID = 2;
    private int mInstanceCount = 0;
    private int mFormsCount = 0;

    private static boolean EXIT = true;

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(i==LOADER_FORMS_ID)
            return new CursorLoader(MainMenuActivity.this, FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);
        else
            return new CursorLoader(MainMenuActivity.this, InstanceColumns.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_FORMS_ID:
                mFormsCount = cursor!=null ? cursor.getCount() : 0;
                break;
            case LOADER_INSTANCES_ID:
                mInstanceCount = cursor!=null ? cursor.getCount() : 0;
                updateButtons();
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> cursorLoader) {
        mFormsCount = 0;
        mInstanceCount = 0;
        invalidateOptionsMenu();
        updateButtons();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // must be at the beginning of any activity that can be called from an external intent
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }
        setContentView(R.layout.main_menu);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothService = (BluetoothService)getLastNonConfigurationInstance();
        //Log.d(TAG, "mBluetoothService onCreate: "+mBluetoothService);

        registerReceiver(BluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        {
            // dynamically construct the "Last Mile Health vA.B" string
            TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
            mainMenuMessageLabel.setText(Collect.getInstance().getVersionedAppName());
        }
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        File f = new File(Collect.ODK_ROOT + "/collect.settings");
        if (f.exists()) {
            boolean success = loadSharedPreferencesFromFile(f);
            if (success) {
                Toast.makeText(this,
                        "Settings successfully loaded from file",
                        Toast.LENGTH_LONG).show();
                f.delete();
            } else {
                Toast.makeText(
                        this,
                        "Sorry, settings file is corrupt and should be deleted or replaced",
                        Toast.LENGTH_LONG).show();
            }
        }

        mAdminPreferences = this.getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        //mEnterDataButton.setText(getString(R.string.enter_data_button));
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFillForm();
            }
        });

        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        //mReviewDataButton.setText(getString(R.string.review_data_button));
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "editSavedForm", "click");
                Intent i = new Intent(getApplicationContext(),
                        InstanceChooserList.class);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        /**mGetFormsButton = (Button) findViewById(R.id.get_forms);
         //mGetFormsButton.setText(getString(R.string.get_forms));
         mGetFormsButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
        Collect.getInstance().getActivityLogger()
        .logAction(this, "downloadBlankForms", "click");
        SharedPreferences sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(MainMenuActivity.this);
        String protocol = sharedPreferences.getString(
        PreferencesActivity.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
        Intent i = null;
        if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_maps_engine))) {
        i = new Intent(getApplicationContext(),
        GoogleDriveActivity.class);
        } else {
        i = new Intent(getApplicationContext(),
        FormDownloadList.class);
        }
        startActivity(i);

        }
        });*/

        // manage forms button. no result expected.
        //mManageFilesButton.setText(getString(R.string.manage_files));

        mCallbacks = this;
        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_FORMS_ID, null, mCallbacks);
        lm.initLoader(LOADER_INSTANCES_ID, null, mCallbacks);
    }

    private void onFillForm(){

        XMLParser parser = new XMLParser(this);

        TinyDB tinyDB = new TinyDB(this);
        ArrayList<String> permission = tinyDB.getListString(AdminPreferencesActivity.CURRENT_PERMISSIONS);
        String[] rolesName = parser.getmNames().toArray(new String[parser.getmNames().size()]); //getRolesName();
        if(permission.isEmpty() && rolesName.length > 0){
            showPopupDialog();
        }else{
            openFillForm();
        }
    }

    private void openFillForm(){
        Collect.getInstance().getActivityLogger()
                .logAction(this, "fillBlankForm", "click");
        Intent i = new Intent(getApplicationContext(),
                FormChooserList.class);
        startActivity(i);
    }

    private void showPopupDialog() {

        final String[] rolesName = getRolesName();

        final TinyDB tinyDB = new TinyDB(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_role)
                .setItems(rolesName, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<String> permission = tinyDB.getListString(rolesName[which]);
                        tinyDB.putListString(AdminPreferencesActivity.CURRENT_PERMISSIONS, permission);

                        tinyDB.putString(AdminPreferencesActivity.CURRENT_ROLE, rolesName[which]);

                        openFillForm();
                    }
                });
        builder.create();
        builder.show();
    }

    public String[] getRolesName() {
        TinyDB tinydb = new TinyDB(this);
        String[] rolesName = tinydb.getListString(AdminPreferencesActivity.ROLES).toArray(new String[tinydb.getListString(AdminPreferencesActivity.ROLES).size()]);
        return rolesName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        com.github.snowdream.android.util.Log.d(MainMenuActivity.class.getName(), "[onActivityResult] requestCode : " + requestCode);

        switch (requestCode) {
            case REQUEST_MAKE_DISCOVERABLE:
                if(resultCode == Activity.RESULT_CANCELED) {
                    //Toast.makeText(MainMenuActivity.this, getString(R.string.no_update), Toast.LENGTH_LONG).show();
                } else {
                    start_accept();
                }
                break;
            case REQUEST_ENABLE_BT_FORMS:
                if(resultCode == Activity.RESULT_OK) {
                    Intent listIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_FORMS);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_bt_enabled), Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_ENABLE_BT_INSTANCES:
                if(resultCode == Activity.RESULT_OK) {
                    Intent listIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_INSTANCES);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_bt_enabled), Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CONNECT_DEVICE_FORMS:
                if(resultCode == Activity.RESULT_OK) {
                    String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Log.d(TAG, "device selected: "+address);

                    Intent showProgress = new Intent(getApplicationContext(), ProgressActivity.class);
                    showProgress.putExtra(ProgressActivity.DEVICE_NAME_PARAM, name);
                    startActivity(showProgress);

                    // Get the BluetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    start_connect(device, true);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_device_selected), Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSTANCES:
                if(resultCode == Activity.RESULT_OK) {
                    String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Log.d(TAG, "device selected: "+address);

                    Intent showProgress = new Intent(getApplicationContext(), ProgressActivity.class);
                    showProgress.putExtra(ProgressActivity.DEVICE_NAME_PARAM, name);
                    startActivity(showProgress);

                    // Get the BluetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    start_connect(device, false);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_device_selected), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void start_accept() {
        if(mBluetoothService == null)
            mBluetoothService = new BluetoothService(mHandler, this);
        mBluetoothService.stop();
        mBluetoothService.startAccept();
    }

    private void start_connect(BluetoothDevice device, boolean isForms) {

        com.github.snowdream.android.util.Log.d(MainMenuActivity.class.getName(), "[start_connect] device : " + device.getName());

        if(mBluetoothService == null)
            mBluetoothService = new BluetoothService(mHandler, this);

        if(isForms)
            prepareForms();
        else
            prepareInstances();

        if(new File(Collect.ZIP_PATH).exists())
            mBluetoothService.connect(device, isForms);
        else {
            if(isForms)
                Toast.makeText(MainMenuActivity.this, getString(R.string.no_forms), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainMenuActivity.this, getString(R.string.no_instances), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mBluetoothService;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BluetoothStateChangedReceiver);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
                        .setIcon(R.drawable.ic_menu_preferences),
                MenuItem.SHOW_AS_ACTION_NEVER);
        CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_ADMIN, 0, R.string.admin_preferences)
                        .setIcon(R.drawable.ic_menu_login),
                MenuItem.SHOW_AS_ACTION_NEVER);
        CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_APP_UPDATE, 0, R.string.receive_forms_updates),
                MenuItem.SHOW_AS_ACTION_NEVER);
        CompatibilityUtils.setShowAsAction(
                menu.add(0, SEND_FORMS, 0, getString(R.string.send_update) + " (" + mFormsCount + ")"),
                MenuItem.SHOW_AS_ACTION_NEVER);
        CompatibilityUtils.setShowAsAction(
                menu.add(0, SEND_INSTANCES, 0, getString(R.string.send_completed_forms) + " (" + mInstanceCount + ")"),
                MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Intent ig = new Intent(this, PreferencesActivity.class);
                startActivity(ig);
                return true;
            case MENU_ADMIN:
                String pw = mAdminPreferences.getString(
                        AdminPreferencesActivity.KEY_ADMIN_PW, "");
                if ("".equalsIgnoreCase(pw)) {
                    Intent i = new Intent(getApplicationContext(),
                            AdminPreferencesActivity.class);
                    startActivity(i);
                } else {
                    showDialog(PASSWORD_DIALOG);
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "createAdminPasswordDialog", "show");
                }
                return true;
            case MENU_APP_UPDATE:
                if (mBluetoothAdapter == null) {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_bt_available), Toast.LENGTH_LONG).show();
                } else if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
                    startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.already_ready), Toast.LENGTH_LONG).show();
                    start_accept();
                }
                return true;
            case SEND_FORMS:
                if(mFormsCount>0) {
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(MainMenuActivity.this, getString(R.string.no_bt_available), Toast.LENGTH_LONG).show();
                    } else if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT_FORMS);
                    } else {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();

                        if (mBluetoothService != null) mBluetoothService.stop();
                        Intent listIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                        startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_FORMS);
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_forms), Toast.LENGTH_SHORT).show();
                }
                return true;
            case SEND_INSTANCES:
                if(mInstanceCount>0) {
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(MainMenuActivity.this, getString(R.string.no_bt_available), Toast.LENGTH_LONG).show();
                    } else if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT_INSTANCES);
                    } else {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();

                        if (mBluetoothService != null) mBluetoothService.stop();
                        Intent listIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                        startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_INSTANCES);
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.no_instances), Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger()
                .logAction(this, "createErrorDialog", "show");
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance()
                                .getActivityLogger()
                                .logAction(this, "createErrorDialog",
                                        shouldExit ? "exitApplication" : "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PASSWORD_DIALOG:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog passwordDialog = builder.create();

                passwordDialog.setTitle(getString(R.string.enter_admin_password));
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
                passwordDialog.setView(input, 20, 10, 20, 10);

                passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                String pw = mAdminPreferences.getString(
                                        AdminPreferencesActivity.KEY_ADMIN_PW, "");
                                if (pw.compareTo(value) == 0) {
                                    Intent i = new Intent(getApplicationContext(),
                                            AdminPreferencesActivity.class);
                                    startActivity(i);
                                    input.setText("");
                                    passwordDialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            MainMenuActivity.this,
                                            getString(R.string.admin_password_incorrect),
                                            Toast.LENGTH_SHORT).show();
                                    Collect.getInstance()
                                            .getActivityLogger()
                                            .logAction(this, "adminPasswordDialog",
                                                    "PASSWORD_INCORRECT");
                                }
                            }
                        });

                passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logAction(this, "adminPasswordDialog",
                                                "cancel");
                                input.setText("");
                                return;
                            }
                        });

                passwordDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return passwordDialog;

        }
        return null;
    }

    private void updateButtons() {
        if(mInstanceCount > 0) {
            mReviewDataButton.setText(getString(R.string.review_data_button, mInstanceCount));
        } else {
            mReviewDataButton.setText(getString(R.string.review_data));
        }
    }

    private boolean loadSharedPreferencesFromFile(File src) {
        // this should probably be in a thread if it ever gets big
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(
                    this).edit();
            prefEdit.clear();
            // first object is preferences
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();

            // second object is admin options
            Editor adminEdit = getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).edit();
            adminEdit.clear();
            // first object is preferences
            Map<String, ?> adminEntries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : adminEntries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    adminEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    adminEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    adminEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    adminEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    adminEdit.putString(key, ((String) v));
            }
            adminEdit.commit();

            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    private void prepareForms() {
        try {
            ZipUtils.zipForms(Collect.ZIP_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void prepareInstances() {
        try {
            ZipUtils.zipInstances(Collect.ZIP_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver BluetoothStateChangedReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)== BluetoothAdapter.STATE_OFF) {
                    if(mBluetoothService!=null && mBluetoothService.getState()==mBluetoothService.STATE_CONNECTED) {
                        return;
                    }
                    Toast.makeText(context, getString(R.string.no_bt_enabled), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}
