package com.w3.meshlib.bluetooth;

public interface BleMeshListener {
    void onErrorOccurred(String msg);
    void onMessageReceived(String message, BleLink bleLink);

}
