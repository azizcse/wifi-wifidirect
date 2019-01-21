package com.w3.meshlib.common.listeners;


import com.w3.meshlib.common.GroupServiceDevice;
import com.w3.meshlib.common.WiFiP2PError;

import java.util.List;

public interface ServiceDiscoveredListener {

    void onNewServiceDeviceDiscovered(GroupServiceDevice serviceDevice);

    void onFinishServiceDeviceDiscovered(List<GroupServiceDevice> serviceDevices);

    void onError(WiFiP2PError wiFiP2PError);

}
