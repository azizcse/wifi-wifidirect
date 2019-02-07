package com.w3.meshlib.model;

import com.w3.meshlib.bluetooth.BleLink;
import com.w3.meshlib.common.GroupDevice;

public class BluetoothUser extends GroupDevice {
    private String userName;
    private String userId;
    private BleLink bleLink;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BleLink getBleLink() {
        return bleLink;
    }

    public void setBleLink(BleLink bleLink) {
        this.bleLink = bleLink;
    }
}
