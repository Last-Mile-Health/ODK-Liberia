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

package org.lastmilehealth.collect.android.utilities;

import java.util.UUID;

/**
 * Defines several constants used between {@link .tasks.BluetoothService} and the UI.
 */
public interface Constants {
    public static final String NAME_SECURE = "BluetoothService";
    public static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_DATA_SUCCESS_SENT = 1;
    public static final int MESSAGE_DATA_NOT_SENDED =2;
    public static final int MESSAGE_DATA_RECEIVED = 3;
    public static final int MESSAGE_DATA_PROCESSED = 4;
    public static final int MESSAGE_DATA_PROCESS_ERROR = 5;
    public static final int MESSAGE_CONNECTION_LIMIT = 6;
    public static final int MESSAGE_CONNECTED = 7;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String IS_CLIENT = "is_client";
}
