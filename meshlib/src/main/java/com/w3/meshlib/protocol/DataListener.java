package com.w3.meshlib.protocol;

import android.net.wifi.p2p.WifiP2pDevice;

public interface DataListener {
    void onMessageReceived(String msg, String ipAddress);
}
