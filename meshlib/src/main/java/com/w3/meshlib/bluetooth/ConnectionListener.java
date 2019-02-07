package com.w3.meshlib.bluetooth;

public interface ConnectionListener {
    void read(BleLink link, String data);
    void close(BleLink link);
    void onConnectionClosed(String mac);
}

