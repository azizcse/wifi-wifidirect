package com.w3.meshlib.service;


import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.WiFiDirectBroadcastReceiver;
import com.w3.meshlib.common.WiFiP2PError;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.common.direct.WiFiDirectUtils;
import com.w3.meshlib.common.listeners.ClientConnectedListener;
import com.w3.meshlib.common.listeners.ClientDisconnectedListener;
import com.w3.meshlib.common.listeners.DataReceivedListener;
import com.w3.meshlib.common.listeners.ConnectionInfoListener;
import com.w3.meshlib.common.listeners.ServiceRegisteredListener;
import com.w3.meshlib.common.messages.MessageWrapper;
import com.w3.meshlib.util.HandlerUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * Singleton class acting as a "server" device.
 * <p>
 * With Wroup Library you can register a service in the current local network to be discovered by
 * other devices. When a service is registered a WiFi P2P Group is created, we know it as Wroup ;)
 * <p>
 * <code>WiFiDirectService</code> is the group owner and it manages the group changes (connections and
 * disconnections). When a new client is connected/disconnected the service device notify to the
 * other devices connected.
 * <p>
 * To register a service you must do the following:
 * <pre>
 * {@code
 *
 * wiFiP2PService = WiFiDirectService.getInstance(getApplicationContext());
 * wiFiP2PService.registerService(groupName, new ServiceRegisteredListener() {
 *
 *  public void onSuccessServiceRegistered() {
 *      Log.i(TAG, "Wroup created. Waiting for client connections...");
 *  }
 *
 *  public void onErrorServiceRegistered(WiFiP2PError wiFiP2PError) {
 *      Log.e(TAG, "Error creating group");
 *  }
 *
 * });
 * }
 * </pre>
 */
public class WiFiDirectService implements ConnectionInfoListener {


    private static final String TAG = "WificonnectionTest";

    private static final String SERVICE_TYPE = "_wroup._tcp";

    private Map<String, GroupDevice> clientsConnected = new HashMap<>();
    private WiFiP2PInstance wiFiP2PInstance;
    private WiFiDirectBroadcastReceiver broadcastReceiver;
    private Boolean groupAlreadyCreated = false;
    private Context mContext;
    private String myNodeId;

    public WiFiDirectService(Context context, String userId) {
        this.mContext = context;
        this.myNodeId = userId;
        wiFiP2PInstance = WiFiP2PInstance.getInstance(context);
        wiFiP2PInstance.setPeerConnectedListener(this);
        broadcastReceiver = wiFiP2PInstance.getBroadcastReceiver();
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }


    public void registerService() {
        removeAndCreateGroup();
    }

    private void removeAndCreateGroup() {
        wiFiP2PInstance.getWifiP2pManager().requestGroupInfo(wiFiP2PInstance.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(final WifiP2pGroup group) {
                if (group != null) {
                    wiFiP2PInstance.getWifiP2pManager().removeGroup(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            groupAlreadyCreated = false;
                            // Now we can create the group
                            createGroup();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.e(TAG, "Error deleting group");
                        }
                    });
                } else {
                    createGroup();
                }
            }
        });
    }

    private void createGroup() {
        if (!groupAlreadyCreated) {
            wiFiP2PInstance.getWifiP2pManager().createGroup(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, "Group created!");
                    groupAlreadyCreated = true;
                    //requestQroupInfoForAdvertising();

                    HandlerUtil.postForeground(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "GO Created", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Error creating group. Reason: " + WiFiP2PError.fromReason(reason));

                    HandlerUtil.postForeground(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "GO creation failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.i(TAG, "Im Go OnPeerConnected...");

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.i(TAG, "I am the group owner");
            Log.i(TAG, "My addess is: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
            requestQroupInfoForAdvertising();
        }
    }


    private void requestQroupInfoForAdvertising() {
        wiFiP2PInstance.getWifiP2pManager().requestGroupInfo(wiFiP2PInstance.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    Log.e(TAG, "SSID name...... : " + group.getNetworkName());
                    Log.e(TAG, "Password........ : " + group.getPassphrase());
                    startServiceBroadcasting(group.getNetworkName(), group.getPassphrase());
                } else {
                    Log.e(TAG, "No ssid name SSID null.......... : ");
                }
            }
        });
    }

    private boolean startServiceBroadcasting(String ssId, String password) {
        Map<String, String> record = new HashMap<>();
        record.put("available", "visible");
        record.put("ss_id", ssId);
        record.put("pa_ss", password);
        Log.e(TAG, "Advertise local service triggered........");

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("mesh", SERVICE_TYPE, record);
        wiFiP2PInstance.getWifiP2pManager().addLocalService(wiFiP2PInstance.getChannel(), serviceInfo, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service registered success  +++++");

            }

            @Override
            public void onFailure(int reason) {
                WiFiP2PError wiFiP2PError = WiFiP2PError.fromReason(reason);
                if (wiFiP2PError != null) {
                    Log.e(TAG, "Failure registering the service. Reason: " + wiFiP2PError.name());

                }
            }

        });

        return false;
    }

    public void stopGoAllEvent() {
        clearLocalServices();
        removeGroup();
        stopPeerDiscovering();
    }


    public void clearLocalServices() {
        wiFiP2PInstance.getWifiP2pManager().clearLocalServices(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Local services cleared");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error clearing local services: " + WiFiP2PError.fromReason(reason));
            }

        });
    }

    private void removeGroup() {
        wiFiP2PInstance.getWifiP2pManager().requestGroupInfo(wiFiP2PInstance.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(final WifiP2pGroup group) {
                wiFiP2PInstance.getWifiP2pManager().removeGroup(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        groupAlreadyCreated = false;
                        Log.i(TAG, "Group removed: " + group.getNetworkName());
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "Fail disconnecting from group. Reason: " + WiFiP2PError.fromReason(reason));
                    }
                });
            }
        });
    }

    private void stopPeerDiscovering() {
        wiFiP2PInstance.getWifiP2pManager().stopPeerDiscovery(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Peer disconvering stopped");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error stopping peer discovering: " + WiFiP2PError.fromReason(reason));
            }
        });
    }

}
