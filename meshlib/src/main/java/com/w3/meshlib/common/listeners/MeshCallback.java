package com.w3.meshlib.common.listeners;

import com.w3.meshlib.model.BluetoothUser;
import com.w3.meshlib.model.User;

public interface MeshCallback {
    void onDeviceInfoUpdated(String deviceName, boolean isMaster);

    void onUserDiscovered(User user);

    //void onBleUserFound(BluetoothUser bluetoothUser);

    void onMessageReceived(String message, String userId);
}
