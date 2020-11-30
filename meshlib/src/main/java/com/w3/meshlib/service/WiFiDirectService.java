package com.w3.meshlib.service;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.WiFiP2PError;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.common.direct.WiFiDirectUtils;
import com.w3.meshlib.common.listeners.ClientConnectedListener;
import com.w3.meshlib.common.listeners.ClientDisconnectedListener;
import com.w3.meshlib.common.listeners.DataReceivedListener;
import com.w3.meshlib.common.listeners.ConnectionInfoListener;
import com.w3.meshlib.common.listeners.ServiceRegisteredListener;
import com.w3.meshlib.common.messages.MessageWrapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

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


    private static final String TAG = WiFiDirectService.class.getSimpleName();

    private static final String SERVICE_TYPE = "_wroup._tcp";
    public static final String SERVICE_PORT_PROPERTY = "SERVICE_PORT";
    public static final Integer SERVICE_PORT_VALUE = 9999;
    public static final String SERVICE_NAME_PROPERTY = "SERVICE_NAME";
    public static final String SERVICE_NAME_VALUE = "WROUP";
    public static final String SERVICE_GROUP_NAME = "GROUP_NAME";

    private static WiFiDirectService instance;

    private DataReceivedListener dataReceivedListener;
    private ClientConnectedListener clientConnectedListener;
    private ClientDisconnectedListener clientDisconnectedListener;
    private Map<String, GroupDevice> clientsConnected = new HashMap<>();
    private WiFiP2PInstance wiFiP2PInstance;

    private ServerSocket serverSocket;
    private Boolean groupAlreadyCreated = false;

    private WiFiDirectService(Context context) {
        wiFiP2PInstance = WiFiP2PInstance.getInstance(context);
        wiFiP2PInstance.setPeerConnectedListener(this);
    }

    /**
     * Return the <code>WiFiDirectService</code> instance. If the instance doesn't exist yet, it's
     * created and returned.
     *
     * @param context The application context.
     * @return The actual <code>WiFiDirectService</code> instance.
     */
    public static WiFiDirectService getInstance(Context context) {
        if (instance == null) {
            instance = new WiFiDirectService(context);
        }
        return instance;
    }

    /**
     * Start a Wroup service registration in the actual local network with the name indicated in
     * the arguments. When te service is registered the method
     * {@link ServiceRegisteredListener#onSuccessServiceRegistered()} is called.
     *
     * @param groupName                 The name of the group that want to be created.
     * @param serviceRegisteredListener The <code>ServiceRegisteredListener</code> to notify
     *                                  registration changes.
     */
    public void registerService(String groupName, ServiceRegisteredListener serviceRegisteredListener) {
        registerService(groupName, null, serviceRegisteredListener);
    }

    /**
     * Start a Wroup service registration in the actual local network with the name indicated in
     * the arguments. When te service is registered the method
     * {@link ServiceRegisteredListener#onSuccessServiceRegistered()} is called.
     *
     * @param groupName                 The name of the group that want to be created.
     * @param customProperties          A Map of custom properties which will be registered with the
     *                                  service. This properties can be accessed by the client devices
     *                                  when the service is discovered.
     * @param serviceRegisteredListener The <code>ServiceRegisteredListener</code> to notify
     *                                  registration changes.
     */
    public void registerService(String groupName, Map<String, String> customProperties, final ServiceRegisteredListener serviceRegisteredListener) {

        // We need to start peer discovering because otherwise the clients cannot found the service
        /*wiFiP2PInstance.startPeerDiscovering();

        Map<String, String> record = new HashMap<>();
        record.put(SERVICE_PORT_PROPERTY, SERVICE_PORT_VALUE.toString());
        record.put(SERVICE_NAME_PROPERTY, SERVICE_NAME_VALUE);
        record.put(SERVICE_GROUP_NAME, groupName);

        // Insert the custom properties to the record Map
        if (customProperties != null) {
            for (Map.Entry<String, String> entry : customProperties.entrySet()) {
                record.put(entry.getKey(), entry.getValue());
            }
        }

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(groupName, SERVICE_TYPE, record);

        wiFiP2PInstance.getWifiP2pManager().clearLocalServices(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Success clearing local services");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error clearing local services: " + reason);
            }
        });

        wiFiP2PInstance.getWifiP2pManager().addLocalService(wiFiP2PInstance.getChannel(), serviceInfo, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service registered");
                serviceRegisteredListener.onSuccessServiceRegistered();
                removeAndCreateGroup();
            }

            @Override
            public void onFailure(int reason) {
                WiFiP2PError wiFiP2PError = WiFiP2PError.fromReason(reason);
                if (wiFiP2PError != null) {
                    Log.e(TAG, "Failure registering the service. Reason: " + wiFiP2PError.name());
                    serviceRegisteredListener.onErrorServiceRegistered(wiFiP2PError);
                }
            }

        });
*/
        removeAndCreateGroup();
    }

    /**
     * Remove the group created. Before the disconnection, the server sends a message to all
     * clients connected to notify the disconnection.
     */
    public void disconnect() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                Log.i(TAG, "ServerSocket closed");
            } catch (IOException e) {
                Log.e(TAG, "Error closing the serverSocket");
            }
        }

        groupAlreadyCreated = false;
        serverSocket = null;
        clientsConnected.clear();

        WiFiDirectUtils.removeGroup(wiFiP2PInstance);
        WiFiDirectUtils.clearLocalServices(wiFiP2PInstance);
        WiFiDirectUtils.stopPeerDiscovering(wiFiP2PInstance);
    }

    /**
     * Set the listener to know when data is received from the client devices connected to the group.
     *
     * @param dataReceivedListener The <code>DataReceivedListener</code> to notify data entries.
     */
    public void setDataReceivedListener(DataReceivedListener dataReceivedListener) {
        this.dataReceivedListener = dataReceivedListener;
    }

    /**
     * Set the listener to know when a client has been disconnected from the group.
     *
     * @param clientDisconnectedListener The <code>ClientDisconnectedListener</code> to notify
     *                                   client disconnections.
     */
    public void setClientDisconnectedListener(ClientDisconnectedListener clientDisconnectedListener) {
        this.clientDisconnectedListener = clientDisconnectedListener;
    }

    /**
     * Set the listener to know when a new client is registered in the group.
     *
     * @param clientConnectedListener The <code>ClientConnectedListener</code> to notify new
     *                                 connections in the group.
     */
    public void setClientConnectedListener(ClientConnectedListener clientConnectedListener) {
        this.clientConnectedListener = clientConnectedListener;
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


    private void requestQroupInfoForAdvertising(){
        wiFiP2PInstance.getWifiP2pManager().requestGroupInfo(wiFiP2PInstance.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    Log.e(TAG, "SSID name...... : " + group.getNetworkName());
                    Log.e(TAG, "Password........ : " + group.getPassphrase());
                    startServiceBroadcasting(group.getNetworkName(), group.getPassphrase());
                }else {
                    Log.e(TAG, "No ssid name SSID null.......... : ");
                }
            }
        });
    }

    private boolean startServiceBroadcasting(String ssId, String password) {
        Map<String, String> record = new HashMap<>();
        record.put("available", "visible");
        record.put("ssid", ssId);
        record.put("pass",password);

        Log.e(TAG, "Advertise local service triggered........");

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("small", SERVICE_TYPE, record);
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



    /**
     * Send a message to all the devices connected to the group.
     *
     * @param message The message to be sent.
     */
    public void sendMessageToAllClients(final MessageWrapper message) {
        for (GroupDevice clientDevice : clientsConnected.values()) {
            sendMessage(clientDevice, message);
        }
    }

    /**
     * Send a message to the desired device who it's connected in the group.
     *
     * @param device  The receiver of the message.
     * @param message The message to be sent.
     */
    public void sendMessage(final GroupDevice device, MessageWrapper message) {

    }



    private void removeAndCreateGroup() {
        wiFiP2PInstance.getWifiP2pManager().requestGroupInfo(wiFiP2PInstance.getChannel(), new WifiP2pManager.GroupInfoListener() {

            @Override
            public void onGroupInfoAvailable(final WifiP2pGroup group) {
                if (group != null) {
                    wiFiP2PInstance.getWifiP2pManager().removeGroup(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Group deleted");
                            Log.d(TAG, "\tNetwordk Name: " + group.getNetworkName());
                            Log.d(TAG, "\tInterface: " + group.getInterface());
                            Log.d(TAG, "\tPassword: " + group.getPassphrase());
                            Log.d(TAG, "\tOwner Name: " + group.getOwner().deviceName);
                            Log.d(TAG, "\tOwner Address: " + group.getOwner().deviceAddress);
                            Log.d(TAG, "\tClient list size: " + group.getClientList().size());

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
                    requestQroupInfoForAdvertising();
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Error creating group. Reason: " + WiFiP2PError.fromReason(reason));
                }
            });
        }
    }


}
