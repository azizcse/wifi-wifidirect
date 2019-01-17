package com.w3engineers.meshrnd.wifidirect;

import com.w3engineers.meshrnd.model.UserModel;

public interface WifiDirectDataListener {
    void onUserFound(UserModel userModel);
    void sendResponseInfo(String ipAddress);
}
