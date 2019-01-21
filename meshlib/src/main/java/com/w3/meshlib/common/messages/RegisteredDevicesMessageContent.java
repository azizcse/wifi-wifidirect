package com.w3.meshlib.common.messages;


import com.w3.meshlib.common.GroupDevice;

import java.util.List;

public class RegisteredDevicesMessageContent {

    private List<GroupDevice> devicesRegistered;

    public List<GroupDevice> getDevicesRegistered() {
        return devicesRegistered;
    }

    public void setDevicesRegistered(List<GroupDevice> devicesRegistered) {
        this.devicesRegistered = devicesRegistered;
    }

}
