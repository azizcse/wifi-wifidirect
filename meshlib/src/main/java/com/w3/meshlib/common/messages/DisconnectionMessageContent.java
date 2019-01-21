package com.w3.meshlib.common.messages;


import com.w3.meshlib.common.GroupDevice;


public class DisconnectionMessageContent {

    private GroupDevice wroupDevice;


    public void setWroupDevice(GroupDevice wroupDevice) {
        this.wroupDevice = wroupDevice;
    }

    public GroupDevice getWroupDevice() {
        return wroupDevice;
    }

}
