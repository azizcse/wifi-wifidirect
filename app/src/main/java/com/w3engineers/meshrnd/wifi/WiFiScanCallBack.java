package com.w3engineers.meshrnd.wifi;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/15/2019 at 3:43 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/15/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.net.wifi.p2p.WifiP2pDevice;

import com.w3engineers.meshrnd.model.UserModel;

import java.util.List;

public interface WiFiScanCallBack {
    void onScanFinish();
    void onUserFound(UserModel userModel);
    void onUserFound(List<UserModel> userModels);
    void updateDeviceAddress();
}
