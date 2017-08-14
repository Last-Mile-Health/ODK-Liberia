/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lastmilehealth.collect.android.tasks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.github.snowdream.android.util.Log;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.listeners.DiskSyncListener;
import org.lastmilehealth.collect.android.parser.TinyDB;
import org.lastmilehealth.collect.android.preferences.AdminPreferencesActivity;
import org.lastmilehealth.collect.android.provider.InstanceProvider;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;
import org.lastmilehealth.collect.android.utilities.Constants;
import org.lastmilehealth.collect.android.utilities.FileUtils;
import org.lastmilehealth.collect.android.utilities.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
    private static final int CONNECTION_LIMIT = 30; //seconds
    int BIG_NUM = 4192;
    private static final int headerLength = 4;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;

    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private boolean isClient = true; //false if connected from AcceptThread
    private boolean isForms = true; //false if sending forms

    private int mState;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private CountDownTimer cdt;
    private Context mContext;
    private static final String ROLES = "roles";


    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Handler handler, Context context) {
        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mState = STATE_NONE;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "BTS setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void startAccept() {
        Log.d(TAG, "BTS startAccept");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
        }
        mSecureAcceptThread.start();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device, boolean isForms) {
        Log.d(TAG, "BTS connect to: " + device);
        this.isForms = isForms;

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);

        //time limit for connection
        cdt = new CountDownTimer(CONNECTION_LIMIT *10000,10000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                if(getState()==STATE_CONNECTING) {
                    stop();

                    Message msg = mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_LIMIT);
                    mHandler.sendMessage(msg);
                }
            }
        };
        cdt.start();
    }


    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "BTS connected!");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);

        //notify that we connected now
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_CONNECTED);
        mHandler.sendMessage(msg);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "BTS stop");

        if(cdt!=null)
            cdt.cancel();

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public boolean writeFile(File file) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return false;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized

        byte[] bytes = FileUtils.getFileAsBytes(file);
        if(bytes != null)
            return r.write(bytes);

        return false;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "listenUsingRfcommWithServiceRecord failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            Log.d(TAG, "BEGIN accept" + this);

            org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.ZIP_PATH));
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "socket accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                isClient = false;
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread");

        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "mmServerSocket close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            setName("ConnectThread");
            Log.d(TAG, "BEGIN connect " + this);

            // Always cancel discovery because it will slow down a connection
            if(mAdapter.isDiscovering())
                mAdapter.cancelDiscovery();


            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "createRfcommSocketToServiceRecord failed", e);
            }

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket = mmSocket == null ? (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1) : mmSocket;
            } catch (InvocationTargetException e) {
                Log.e(TAG, "createRfcommSocket failed", e);
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "createRfcommSocket failed", e);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.e(TAG, "createRfcommSocket failed", e);
                e.printStackTrace();
            }

            if(mmSocket != null){
                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();

                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "unable to close() socket during connection failure", e2);
                    }
                    return;
                }
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            isClient = true;

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect  socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            mmInStream = null;
            mmOutStream = null;

            // Get the BluetoothSocket input and output streams
            try {
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectedThread. isClient = "+isClient);

            if(isClient) //start sending outgoing data
            {
                File file = new File(Collect.ZIP_PATH);
                boolean exists = file.exists();
                boolean wrote = writeFile(file);
                if (file!=null && exists && wrote) {
                    Log.d(TAG, "data was writed: "+file.length());
                    org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.ZIP_PATH));

                    try {
                        InputStream inputStream = mmSocket.getInputStream();
                        while (true) {
                            Log.d(TAG, "waiting for confirmation");
                            byte[] confirmation = new byte[headerLength];
                            inputStream.read(confirmation);
                            Log.d(TAG, "confirmation readed : "+byteArrayToInt(confirmation));


                            if(isForms) { //do nothing, no need to delete forms
                                //clearForms();
                            } else { //delete instances from db and file system
                                SQLiteDatabase db = SQLiteDatabase.openDatabase(Collect.INSTANCES_DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                                db.execSQL("delete from instances");
                                db.close();

                                org.apache.commons.io.FileUtils.cleanDirectory(new File(Collect.INSTANCES_PATH));
                            }

                            //let activity know we are success
                            Message msg = mHandler.obtainMessage(Constants.MESSAGE_DATA_SUCCESS_SENT);
                            mHandler.sendMessage(msg);
                            return;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Reading confirmation failed: "+e);
                    }
                }
                // Notify UI Activity that data was not sended
                Message msg = mHandler.obtainMessage(Constants.MESSAGE_DATA_NOT_SENDED);
                mHandler.sendMessage(msg);
            }
            else //start listening for incoming data
            {
                Log.d("~", "start listening");
                try {
                    File file = new File(Collect.ZIP_PATH);
                    org.apache.commons.io.FileUtils.deleteQuietly(file);
                    OutputStream output = new FileOutputStream(file, false);

                    boolean waitingForHeader = true;
                    byte[] headerBytes = new byte[headerLength];
                    int headerIndex = 0;
                    int size = 0;
                    int remainingSize = 0;

                    while (true) {
                        if (waitingForHeader) {
                            byte[] header = new byte[1];
                            mmInStream.read(header, 0, 1);
                            headerBytes[headerIndex++] = header[0];

                            if (headerIndex == headerLength) {
                                size = byteArrayToInt(headerBytes);
                                remainingSize = size;
                                Log.d(TAG, "size received: " + size);

                                waitingForHeader = false;
                            }

                        } else {
                            byte[] buffer = new byte[BIG_NUM];
                            int bytesRead = mmInStream.read(buffer);

                            //Log.v(TAG, "Read " + bytesRead + " bytes into buffer");
                            output.write(buffer, 0, bytesRead);

                            remainingSize -= bytesRead;
                            //Log.d(TAG, "bytesRead: "+bytesRead+" remainingSize: "+remainingSize);
                            if (remainingSize <= 0) {
                                Log.d(TAG, "data received, lets process it");
                                break;
                            }
                        }
                    }

                    output.close();

                    //process received data
                    if(size > 0) {
                        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DATA_RECEIVED);
                        mHandler.sendMessage(msg);

                        try {
                            //send file size as confirmation
                            OutputStream out = mmSocket.getOutputStream();
                            out.write(intToByteArray(size));

                            ZipUtils.unzip(Collect.ZIP_PATH, Collect.TMP_PATH);
                            if(new File(Collect.TMP_PATH, "forms.txt").exists()) {
                                Log.d("~", "FORMS PROCESSING");

                                try {
                                    clearForms();

                                    org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.TMP_PATH, "forms.txt"));
                                    org.apache.commons.io.FileUtils.copyDirectory(new File(Collect.TMP_PATH), new File(Collect.FORMS_PATH));
                                    org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.ZIP_PATH));
                                    org.apache.commons.io.FileUtils.deleteDirectory(new File(Collect.TMP_PATH));


                                    //add to DB now and notify about processing finish
                                    DiskSyncTask mDiskSyncTask = new DiskSyncTask();
                                    mDiskSyncTask.setDiskSyncListener(new DiskSyncListener() {
                                        @Override
                                        public void SyncComplete(String result) {
                                            Log.d(TAG, "SyncComplete");
                                            mHandler.sendMessage(mHandler.obtainMessage(Constants.MESSAGE_DATA_PROCESSED));
                                        }
                                    });
                                    mDiskSyncTask.execute((Void[]) null);

                                } catch (Exception e2) {
                                    Log.e("~", "FORMS PROCESSING error: "+e2.getMessage());
                                }
                            } else {
                                Log.d("~", "INSTANCES PROCESSING");

                                instancesProcessing();

                                mHandler.sendMessage(mHandler.obtainMessage(Constants.MESSAGE_DATA_PROCESSED));
                            }

                            mmInStream.close();
                            out.close();
                        } catch (Exception e) {
                            Log.e(TAG, "unzip error: "+e);
                        }

                    } else {
                        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DATA_PROCESS_ERROR);
                        mHandler.sendMessage(msg);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Read exception: " + e);
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public boolean write(byte[] buffer) {
            try {
                File file = new File(Collect.ODK_ROOT + "/byteLogs.txt");
                file.createNewFile();


                OutputStream outputStream = new FileOutputStream(file);

                ByteBuffer byteBuffer = ByteBuffer.allocate(headerLength);
                Log.e(TAG,"headerLength size"+ buffer.length );
                byteBuffer.putInt(buffer.length);
                mmOutStream.write(byteBuffer.array());

                //write data
                for (int i = 0; i < buffer.length; i += BIG_NUM) {
                    int b = ((i + BIG_NUM) < buffer.length) ? BIG_NUM : buffer.length - i;
                    mmOutStream.write(Arrays.copyOfRange(buffer, i, i+b));
                    outputStream.write(Arrays.copyOfRange(buffer, i, i + b));

                    Log.e(TAG, " i= " + i + "  b= "+b+i);
                }

                try{
                    outputStream.close();
                }catch (IOException e) {/*don't care about logs*/}

                mmOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write, BT connection failed", e);
                return false;
            }

            return true;
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private void clearForms() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(Collect.FORMS_DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL("delete from forms");
        db.close();

        deletePreferences();
        try {
            org.apache.commons.io.FileUtils.cleanDirectory(new File(Collect.FORMS_PATH));
            org.apache.commons.io.FileUtils.cleanDirectory(new File(Collect.ROLES_PATH));
        } catch (Exception e)  {}
    }

    private void deletePreferences() {
        TinyDB tinydb = new TinyDB(mContext);
//        String[] rolesName = tinydb.getListString(ROLES).toArray(new String[tinydb.getListString(ROLES).size()]);

        ArrayList<String> rolesName = tinydb.getListString(ROLES);

        Log.d("~", "[deletePreferences] roles length : " + rolesName.size());

        for (int i = 0; i < rolesName.size(); i++) {
            Log.d("~", "[deletePreferences] remove role : " + rolesName.get(i));

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
//            preferences.edit().remove(rolesName.get(i)).commit();
            tinydb.remove(rolesName.get(i));
        }
        tinydb.remove(ROLES);
        tinydb.remove(AdminPreferencesActivity.CURRENT_PERMISSIONS);
        tinydb.remove(AdminPreferencesActivity.CURRENT_ROLE);
    }

    private void instancesProcessing() {
        //fill db with new values
        String dbpath = Collect.TMP_PATH+"/instances.db";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbpath, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query(InstanceProvider.INSTANCES_TABLE_NAME, null, null, null, null, null, null);

        Log.d("~", "cursor.getCount(): " + cursor.getCount());

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String newInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
            String instanceFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
            String newFilePath;

            if(new File(instanceFilePath).exists()) { //instance with this path already exist, rare case but not impossible
                newFilePath = getInstanceFilePath(instanceFilePath, 1);
                Log.d(TAG, "instance already exists, new path: "+newFilePath);

                String num = newFilePath.substring(newFilePath.lastIndexOf("(")+1, newFilePath.lastIndexOf(")"));
                newInstanceName += "("+num+")";
                //Log.d(TAG, "newInstanceName: "+newInstanceName);


                final String fromName = instanceFilePath.substring(instanceFilePath.lastIndexOf("instances/") + 10);
                final String toName = newFilePath.substring(instanceFilePath.lastIndexOf("instances/")+10);


                //raname file in tmp folder to prepare for copy direcory
                try {
                    Log.d(TAG, "rename "+fromName+" to "+toName);
                    org.apache.commons.io.FileUtils.copyFile(new File(Collect.TMP_PATH, fromName), new File(Collect.TMP_PATH, toName));
                    org.apache.commons.io.FileUtils.deleteQuietly( new File(Collect.TMP_PATH, fromName));
                } catch (Exception e) {}
            } else {
                // The instances are located in a folder inside instances folder so we are searching for instances/ in the file path and replace it with
                // the path of this device instances dir.
                newFilePath = new File(Collect.INSTANCES_PATH, instanceFilePath.substring(instanceFilePath.lastIndexOf("instances/")+9)).getAbsolutePath();
                Log.d(TAG, "not exist, new path "+newFilePath);
            }

            String submissionUri = null;
            if (!cursor.isNull(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI))) {
                submissionUri = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI));
            }


            //add to db with new name, it it was duplicated
            ContentValues values = new ContentValues();
            values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, newInstanceName);
            values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, submissionUri);
            values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, newFilePath);
            values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID)));
            values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION)));
            values.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_COMPLETE);
            values.put(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, "false");
            values.put(InstanceProviderAPI.InstanceColumns.APP_ID, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.APP_ID)));
            values.put(InstanceProviderAPI.InstanceColumns.IS_TRANSFERRED, 1);

            Log.d(TAG, "insert new instance record: "+newInstanceName+" with path :"+newFilePath);
            Collect.getInstance().getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
        }
        cursor.close();
        db.close();

        //copy directory after deleting metadata, clear all temporary data
        org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.TMP_PATH, "instances.db"));
        org.apache.commons.io.FileUtils.deleteQuietly(new File(Collect.ZIP_PATH));
        try {
            org.apache.commons.io.FileUtils.copyDirectory(new File(Collect.TMP_PATH), new File(Collect.INSTANCES_PATH));
            org.apache.commons.io.FileUtils.deleteDirectory(new File(Collect.TMP_PATH));
        } catch (Exception e) {}
    }

    private String getInstanceFilePath(String oldPath, int tryCount) {
        tryCount++;
        String generatedPath = "";
        if(oldPath.lastIndexOf("(")==-1)
            generatedPath = oldPath.substring(0, oldPath.length()-4)+"("+tryCount+").xml";
        else {
            generatedPath = oldPath.substring(0, oldPath.lastIndexOf("(")) + "(" + tryCount + ").xml";
        }

        if(new File(generatedPath).exists()) {
            return getInstanceFilePath(generatedPath, tryCount);
        }

        return generatedPath;

    }

    private static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    private static int byteArrayToInt(byte[] b) {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }
}