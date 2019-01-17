package com.w3engineers.meshrnd.wifidirect;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/14/2019 at 4:12 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/14/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.HandlerThread;

import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.JsonParser;
import com.w3engineers.meshrnd.wifi.WiFiScanCallBack;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectManager implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener, WifiDirectDataListener {
    /**
     * <p>Instance variable</p>
     */
    private static WifiDirectManager wifiDirectManager;
    private Context context;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifip2pChannel;
    private boolean isWifiDirectConnected = false;
    private HandlerThread handlerThread;
    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private MessageReceiver messageReceiver;
    private MessageSender messageSender;

    private List<UserModel> discoveredUserList;

    private static WiFiScanCallBack wiFiScanCallBack;

    /**
     * <p>private constructor
     *
     * @param context : Context </p>
     */
    private WifiDirectManager(Context context) {
        handlerThread = new HandlerThread("handler_thread");
        handlerThread.start();
        this.context = context;
        discoveredUserList = new ArrayList<>();
        wifiP2pManager = (WifiP2pManager) context.getSystemService(context.WIFI_P2P_SERVICE);
        wifip2pChannel = wifiP2pManager.initialize(context, handlerThread.getLooper(), null);

        //Message receiver init and start
        messageReceiver = new MessageReceiver(context, this);
        messageReceiver.startReceiver();

        //Message sender
        messageSender = new MessageSender();

        //P2P broadcast receiver init
        registerBroadcastReceiver();
    }

    public void stopMsgReceiver() {
        if (messageReceiver != null) {
            messageReceiver.startReceiver();
        }
    }

    public static WifiDirectManager on(Context context, WiFiScanCallBack callBack) {
        if (wifiDirectManager == null) {
            wifiDirectManager = new WifiDirectManager(context);
        }
        wiFiScanCallBack = callBack;
        return wifiDirectManager;
    }

    boolean isConnectionInfoSent = false;

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        AppLog.v("onConnectionInfoAvailable() called");
        if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner && !isConnectionInfoSent) {
            String sendAbleString = JsonParser.getReqString();
            String groupOwnerAddress = wifiP2pInfo.groupOwnerAddress.getHostAddress();
            messageSender.sendMessage(sendAbleString, groupOwnerAddress);
            isConnectionInfoSent = true;
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        AppLog.v("onPeersAvailable() called");
        wiFiScanCallBack.onScanFinish();

        List<WifiP2pDevice> devices = (new ArrayList<>());
        devices.addAll(peerList.getDeviceList());
        for (WifiP2pDevice device : devices) {
            UserModel deviceDTO = new UserModel();
            deviceDTO.setIp(device.deviceAddress);
            deviceDTO.setUserName(device.deviceName);
            deviceDTO.setDeviceName(new String());
            deviceDTO.setOsVersion(new String());
            deviceDTO.setPort(-1);
            discoveredUserList.add(deviceDTO);
            wiFiScanCallBack.onUserFound(deviceDTO);
        }

    }


    public List<UserModel> getUserList() {
        return new ArrayList<>(discoveredUserList);
    }


    /**
     * Called from broadcast receiver
     *
     * @param isEnable
     */
    public void setIsWifiP2pEnabled(boolean isEnable) {

    }

    public void findPeers() {
        if (!isWifiDirectConnected) {
            wifiP2pManager.discoverPeers(wifip2pChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    AppLog.v("User discovery started");
                }

                @Override
                public void onFailure(int reasonCode) {
                    AppLog.v("User discovery failed=" + reasonCode);
                }
            });
        }
    }

    public void registerBroadcastReceiver() {
        IntentFilter wifip2pFilter = new IntentFilter();
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, wifip2pChannel, this);

        AppLog.v("Init P2P broadcast receiver");
        context.registerReceiver(wiFiDirectBroadcastReceiver, wifip2pFilter);
    }

    public void unRegisterReceiver() {
        try {
            AppLog.v("Destroy P2P broadcast receiver");
            context.unregisterReceiver(wiFiDirectBroadcastReceiver);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void createConnection(UserModel model) {
        AppLog.v("createConnection() called");
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = model.getIp();
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 10;
        wifiP2pManager.connect(wifip2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                AppLog.v("createConnection onSuccess()");
            }

            @Override
            public void onFailure(int reasonCode) {
                AppLog.v("createConnection onFailure()");
                // NotificationToast.showToast(LocalDashWiFiDirect.this, "Connection failed. try" +" again: reason: " + reasonCode);
            }
        });
    }

    @Override
    public void onUserFound(UserModel userModel) {
        if (userModel != null) {
            wiFiScanCallBack.onUserFound(userModel);
        }
    }

    @Override
    public void sendResponseInfo(String ipAddress) {
        String responseData = JsonParser.getResString();
        messageSender.sendMessage(responseData, ipAddress);
    }
}
