package com.w3.meshlib.bluetooth;

public interface BleListener {
    void onDeviceConnected(String deviceAddress, BleLink link);
    void onReceivedMessage(BleLink link, String message);
    void onErrorOccurred(String errMsg, String deviceAddress);
    void onConnectionLost(String mac);
}
