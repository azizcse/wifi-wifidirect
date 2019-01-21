package com.w3engineers.meshrnd.wifidirect;

import android.net.wifi.p2p.WifiP2pDevice;

import com.w3engineers.meshrnd.model.UserModel;

import java.util.List;

public interface WifiDirectDataListener {
    void onUserFound(UserModel userModel);
    void onUserFound(List<UserModel> userModel);
    void sendResponseInfo(String ipAddress);
    void onMessageReceived(String msg);
    void updateDeviceInfo(WifiP2pDevice device);

    void onSendListUsers(String toIpAddress);
}
