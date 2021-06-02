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
import android.util.Log;
import android.widget.Toast;

import com.w3engineers.meshrnd.model.Message;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.ui.chat.MessageListener;
import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.HandlerUtil;
import com.w3engineers.meshrnd.util.JsonParser;
import com.w3engineers.meshrnd.util.SharedPref;
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

    private List<UserModel> realUsersDataList;

    private static WiFiScanCallBack wiFiScanCallBack;

    private boolean isGroupOwner = false;

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
        realUsersDataList = new ArrayList<>();
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

    public static WifiDirectManager on(Context context) {
        if (wifiDirectManager == null) {
            wifiDirectManager = new WifiDirectManager(context);
        }

        return wifiDirectManager;
    }

    public void initListener(WiFiScanCallBack callBack) {
        this.wiFiScanCallBack = callBack;
    }

    private MessageListener messageListener;

    public void initMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    boolean isConnectionInfoSent = false;

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        //AppLog.v("onConnectionInfoAvailable() called");


        SharedPref.write(Constants.KEY_STATUS, wifiP2pInfo.isGroupOwner);

        if (wiFiScanCallBack != null) {
            wiFiScanCallBack.updateDeviceAddress();
        }

        Log.i("P2pClient", "onConnectionInfoAvailable called");

        if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
            Log.i("P2pClient", "I am p2p client. Send message to GO");
            String sendAbleString = JsonParser.getReqString();
            String groupOwnerAddress = wifiP2pInfo.groupOwnerAddress.getHostAddress();
            messageSender.sendMessage(sendAbleString, groupOwnerAddress);
            isConnectionInfoSent = true;
        } else {
            AppLog.v("Group woner =" + wifiP2pInfo.toString());
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        //AppLog.v("onPeersAvailable() called =" + peerList.getDeviceList().size());

        if (wiFiScanCallBack != null) {
            wiFiScanCallBack.onScanFinish();

            List<WifiP2pDevice> devices = (new ArrayList<>());
            devices.addAll(peerList.getDeviceList());
            for (WifiP2pDevice device : devices) {
                UserModel deviceDTO = new UserModel();
                deviceDTO.setIp(device.deviceAddress);
                deviceDTO.setUserName(device.deviceName);
                deviceDTO.setDeviceName(new String());
                deviceDTO.setOsVersion(new String());
                deviceDTO.setGroupOwner(device.isGroupOwner());
                deviceDTO.setPort(-1);
                discoveredUserList.add(deviceDTO);
                wiFiScanCallBack.onUserFound(deviceDTO);

                AppLog.v("Group_check", "Peer details =" + device.isGroupOwner());
            }
        }

    }


    public List<UserModel> getUserList() {
        return new ArrayList<>(discoveredUserList);
    }

    private List<UserModel> getRealUserList() {
        return new ArrayList<>(realUsersDataList);
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
                    AppLog.v("WiFi direct discovery started");
                }

                @Override
                public void onFailure(int reasonCode) {
                    AppLog.v("WiFi direct discovery failed=" + reasonCode);
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
        config.groupOwnerIntent = 0;
        wifiP2pManager.connect(wifip2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                AppLog.v("createConnection onSuccess()");
            }

            @Override
            public void onFailure(int reasonCode) {
                AppLog.v("createConnection onFailure() =" + reasonCode);
            }
        });
    }

    @Override
    public void onUserFound(UserModel userModel) {
        realUsersDataList.add(userModel);
        if (userModel != null) {
            if (wiFiScanCallBack != null) {
                wiFiScanCallBack.onUserFound(userModel);
            }
        }
    }

    @Override
    public void onUserFound(List<UserModel> userModels) {
        if (wiFiScanCallBack != null && userModels != null) {
            String myAddress = SharedPref.read(Constants.USER_ID);

            for (int i = 0; i < userModels.size(); i++) {
                if (myAddress.equals(userModels.get(i).getUserId())) {
                    userModels.remove(i);
                    break;
                }
            }

            if (wiFiScanCallBack != null) {
                wiFiScanCallBack.onUserFound(userModels);
            }
        }
    }

    @Override
    public void sendResponseInfo(String ipAddress) {

        String responseData = JsonParser.getResString();
        messageSender.sendMessage(responseData, ipAddress);

    }

    public void sendTextMessage(String ip, Message inputValue) {
        AppLog.v("Send response info ---- ");
        String sendValue = JsonParser.buildMessage(inputValue);
        messageSender.sendMessage(sendValue, ip);
    }

    public void sendTextMessage(String ip, Message inputValue, boolean isP2p) {
        AppLog.v("Send response info ---- ");
        String sendValue = JsonParser.buildMessage(inputValue);

        messageSender.sendMessage(sendValue, ip);
       /* if (isP2p) {
            messageSender.sendMessage(sendValue, ip);
        } else {
            messageSender.sendMessageToServer(sendValue, ip);
        }*/
    }

    @Override
    public void onMessageReceived(String msg) {
        final Message message = JsonParser.parseMessage(msg);
        if (messageListener != null) {
            messageListener.onMessageReceived(message);
        } else {
            HandlerUtil.postForeground(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message.message, Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    @Override
    public void updateDeviceInfo(WifiP2pDevice device) {
        if (wiFiScanCallBack != null) {
            SharedPref.write(Constants.DEVICE_NAME, device.deviceName);
            SharedPref.write(Constants.DEVICE_ADDRESS, device.deviceAddress);
            wiFiScanCallBack.updateDeviceAddress();
            //AppLog.v("Wifi-direct ip="+device.deviceAddress);
        }
    }

    @Override
    public void onSendListUsers(final String toIpAddress) {


        List<UserModel> userModelList = getRealUserList();
        AppLog.v("List user send method called =" + userModelList.size());
        if (userModelList.size() == 0) return;

        String userListString = JsonParser.buildUserListJson(userModelList);
        for (UserModel item : userModelList) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageSender.sendMessage(userListString, item.getIp());
        }


    }


}
