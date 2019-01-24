package com.w3.meshlib.service;


import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Handler;
import android.util.Log;

import com.w3.meshlib.common.Constant;
import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.GroupServiceDevice;
import com.w3.meshlib.common.MeshLog;
import com.w3.meshlib.common.WiFiP2PError;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.common.direct.WiFiDirectUtils;
import com.w3.meshlib.common.listeners.ClientConnectedListener;
import com.w3.meshlib.common.listeners.ClientDisconnectedListener;
import com.w3.meshlib.common.listeners.DataReceivedListener;
import com.w3.meshlib.common.listeners.MeshCallback;
import com.w3.meshlib.common.listeners.PeerConnectedListener;
import com.w3.meshlib.common.listeners.ServiceConnectedListener;
import com.w3.meshlib.common.listeners.ServiceDisconnectedListener;
import com.w3.meshlib.common.listeners.ServiceDiscoveredListener;
import com.w3.meshlib.common.listeners.ServiceRegisteredListener;
import com.w3.meshlib.common.messages.MessageWrapper;
import com.w3.meshlib.model.User;
import com.w3.meshlib.parser.JsonParser;
import com.w3.meshlib.protocol.DataListener;
import com.w3.meshlib.protocol.MessageReceiver;
import com.w3.meshlib.protocol.MessageSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
public class WiFiDirectService implements PeerConnectedListener, ServiceDisconnectedListener, DataListener {


    private static final String TAG = WiFiDirectService.class.getSimpleName();

    private static final String SERVICE_TYPE = "_wroup._tcp";
    public static final String SERVICE_PORT_PROPERTY = "SERVICE_PORT";
    public static final Integer SERVICE_PORT_VALUE = 9999;
    public static final String SERVICE_NAME_PROPERTY = "SERVICE_NAME";
    public static final String SERVICE_NAME_VALUE = "MESH_RND";
    public static final String SERVICE_GROUP_NAME = "GROUP_NAME";
    public static final String DEVELOPER_NAME = "DEVELOPER_NAME";

    private static WiFiDirectService instance;

    private DataReceivedListener dataReceivedListener;
    private ClientConnectedListener clientConnectedListener;
    private ClientDisconnectedListener clientDisconnectedListener;
    private Map<String, GroupDevice> clientsConnected = new HashMap<>();
    private WiFiP2PInstance wiFiP2PInstance;

    private Boolean groupAlreadyCreated = false;

    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private static User mCurrentUser;
    private Map<String, User> mUsersMaps;
    private MeshCallback meshCallback;


    private WiFiDirectService(Context context) {
        wiFiP2PInstance = WiFiP2PInstance.getInstance(context);
        wiFiP2PInstance.setPeerConnectedListener(this);
        wiFiP2PInstance.setServerDisconnectedListener(this);
        messageSender = new MessageSender();
        messageReceiver = new MessageReceiver(context, this);
        messageReceiver.startReceiver();
        mUsersMaps = Collections.synchronizedMap(new HashMap<String, User>());
    }

    public void initMeshCallBack(MeshCallback callback) {
        this.meshCallback = callback;
    }


    public List<User> getConnectedUsers() {
        return new ArrayList<>(mUsersMaps.values());

    }

    /**
     * Return the <code>WiFiDirectService</code> instance. If the instance doesn't exist yet, it's
     * created and returned.
     *
     * @param context The application context.
     * @return The actual <code>WiFiDirectService</code> instance.
     */
    public static WiFiDirectService getInstance(Context context, User user) {
        if (instance == null) {
            instance = new WiFiDirectService(context);
        }
        mCurrentUser = user;
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
        wiFiP2PInstance.startPeerDiscovering();

        Map<String, String> record = new HashMap<>();
        record.put(SERVICE_PORT_PROPERTY, SERVICE_PORT_VALUE.toString());
        record.put(SERVICE_NAME_PROPERTY, SERVICE_NAME_VALUE);
        record.put(SERVICE_GROUP_NAME, groupName);
        //record.put(DEVELOPER_NAME, "Aziz");
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
                MeshLog.v("Success clearing local services");
            }

            @Override
            public void onFailure(int reason) {
                MeshLog.v("Error clearing local services: " + reason);
            }
        });

        wiFiP2PInstance.getWifiP2pManager().addLocalService(wiFiP2PInstance.getChannel(), serviceInfo, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                MeshLog.v("Service registered");
                serviceRegisteredListener.onSuccessServiceRegistered();
                // Create the group to the clients can connect to it
                removeAndCreateGroup();

            }

            @Override
            public void onFailure(int reason) {
                WiFiP2PError wiFiP2PError = WiFiP2PError.fromReason(reason);
                if (wiFiP2PError != null) {
                    MeshLog.v("Failure registering the service. Reason: " + wiFiP2PError.name());
                    serviceRegisteredListener.onErrorServiceRegistered(wiFiP2PError);
                }
            }

        });
    }

    /**
     * Remove the group created. Before the disconnection, the server sends a message to all
     * clients connected to notify the disconnection.
     */
    public void disconnect() {
        /*if (serverSocket != null) {
            try {
                serverSocket.close();
                Log.i(TAG, "ServerSocket closed");
            } catch (IOException e) {
                Log.e(TAG, "Error closing the serverSocket");
            }
        }
        serverSocket = null;
        clientsConnected.clear();*/

        messageReceiver.stopReceiver();

        groupAlreadyCreated = false;
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
     *                                connections in the group.
     */
    public void setClientConnectedListener(ClientConnectedListener clientConnectedListener) {
        this.clientConnectedListener = clientConnectedListener;
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
        messageSender.sendMessage(device.getDeviceServerSocketIP(), message.getMessage());
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
                    MeshLog.v("Group created!");
                    groupAlreadyCreated = true;
                }

                @Override
                public void onFailure(int reason) {
                    MeshLog.v("Error creating group. Reason: " + WiFiP2PError.fromReason(reason));
                }
            });
        }
    }


    @Override
    public void onServerDisconnectedListener() {

    }


    /*********************************************************/
    /******************** Client section *********************/
    /*********************************************************/

    private List<GroupServiceDevice> serviceDevices = new ArrayList<>();
    private WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener;
    private WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener;
    private GroupDevice groupDevice;
    private ServiceConnectedListener serviceConnectedListener;
    private Boolean isRegistered = false;

    /**
     * Start to discover Wroup services registered in the current local network.
     * <p>
     * Before you start to discover services you must to register the <code>WiFiDirectBroadcastReceiver</code>
     * in the <code>onResume()</code> method of your activity.
     *
     * @param discoveringTimeInMillis   The time in milliseconds to search for registered Wroup services.
     * @param serviceDiscoveredListener The listener to notify changes of the services found by the client.
     * @see
     */
    public void discoverServices(Long discoveringTimeInMillis, final ServiceDiscoveredListener serviceDiscoveredListener) {
        serviceDevices.clear();

        // We need to start discovering peers to activate the service search
        wiFiP2PInstance.startPeerDiscovering();

        setupDnsListeners(wiFiP2PInstance, serviceDiscoveredListener);
        WiFiDirectUtils.clearServiceRequest(wiFiP2PInstance);

        WifiP2pServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wiFiP2PInstance.getWifiP2pManager().addServiceRequest(wiFiP2PInstance.getChannel(), serviceRequest, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Success adding service request");
            }

            @Override
            public void onFailure(int reason) {
                WiFiP2PError wiFiP2PError = WiFiP2PError.fromReason(reason);
                Log.e(TAG, "Error adding service request. Reason: " + WiFiP2PError.fromReason(reason));
                serviceDiscoveredListener.onError(wiFiP2PError);
            }

        });

        wiFiP2PInstance.getWifiP2pManager().discoverServices(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Success initiating disconvering services");
            }

            @Override
            public void onFailure(int reason) {
                WiFiP2PError wiFiP2PError = WiFiP2PError.fromReason(reason);
                if (wiFiP2PError != null) {
                    Log.e(TAG, "Error discovering services. Reason: " + wiFiP2PError.name());
                    serviceDiscoveredListener.onError(wiFiP2PError);
                }
            }

        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                serviceDiscoveredListener.onFinishServiceDeviceDiscovered(serviceDevices);
            }
        }, discoveringTimeInMillis);
    }


    /**
     * Start the connection with the <code>GroupServiceDevice</code> passed by argument. When the
     * connection is stablished with the device service the {@link ServiceConnectedListener#onServiceConnected(GroupDevice)}
     * method is called.
     * <p>
     * When the client is connected to the service, it's connected to the WiFi Direct Group created
     * by the service device. Once the client belongs to the "Wroup" (group), it can know when a new
     * client is connected or disconnected from it.
     *
     * @param serviceDevice            The GroupServiceDevice with you want to connect.
     * @param serviceConnectedListener The listener to know when the client device is connected to
     *                                 the desired service.
     */
    public void connectToService(final GroupServiceDevice serviceDevice, ServiceConnectedListener serviceConnectedListener) {
        this.groupDevice = serviceDevice;
        this.serviceConnectedListener = serviceConnectedListener;

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = serviceDevice.getDeviceMac();
        wifiP2pConfig.groupOwnerIntent = 0;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;

        wiFiP2PInstance.getWifiP2pManager().connect(wiFiP2PInstance.getChannel(), wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                MeshLog.v("Initiated connection to device: ");
                MeshLog.v("\tDevice name: " + serviceDevice.getDeviceName());
                MeshLog.v("\tDevice address: " + serviceDevice.getDeviceMac());
            }

            @Override
            public void onFailure(int reason) {
                MeshLog.v("Fail initiation connection. Reason: " + WiFiP2PError.fromReason(reason));
            }
        });
    }


    private void setupDnsListeners(WiFiP2PInstance wiFiP2PInstance, ServiceDiscoveredListener serviceDiscoveredListener) {
        if (dnsSdTxtRecordListener == null || dnsSdServiceResponseListener == null) {
            dnsSdTxtRecordListener = getTxtRecordListener(serviceDiscoveredListener);
            dnsSdServiceResponseListener = getServiceResponseListener();

            wiFiP2PInstance.getWifiP2pManager().setDnsSdResponseListeners(wiFiP2PInstance.getChannel(), dnsSdServiceResponseListener, dnsSdTxtRecordListener);
        }
    }

    private WifiP2pManager.DnsSdTxtRecordListener getTxtRecordListener(final ServiceDiscoveredListener serviceDiscoveredListener) {
        return new WifiP2pManager.DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice device) {

                if (txtRecordMap.containsKey(WiFiDirectService.SERVICE_NAME_PROPERTY)
                        && txtRecordMap.get(WiFiDirectService.SERVICE_NAME_PROPERTY).equalsIgnoreCase(WiFiDirectService.SERVICE_NAME_VALUE)) {

                    Integer servicePort = Integer.valueOf(txtRecordMap.get(WiFiDirectService.SERVICE_PORT_PROPERTY));
                    GroupServiceDevice serviceDevice = new GroupServiceDevice(device);
                    serviceDevice.setDeviceServerSocketPort(servicePort);
                    serviceDevice.setTxtRecordMap(txtRecordMap);
                    serviceDevice.setGroupName(fullDomainName);
                    MeshLog.v("Group name =" + fullDomainName + " mac =" + device.deviceAddress + " device name =" + device.deviceName);
                    if (!serviceDevices.contains(serviceDevice)) {
                        serviceDevices.add(serviceDevice);
                        serviceDiscoveredListener.onNewServiceDeviceDiscovered(serviceDevice);
                    }
                } else {
                    MeshLog.v("New service mac =" + device.deviceAddress + " device name =" + device.deviceName);
                }
            }
        };
    }

    private WifiP2pManager.DnsSdServiceResponseListener getServiceResponseListener() {
        return new WifiP2pManager.DnsSdServiceResponseListener() {

            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

            }
        };
    }

    @Override
    public void onPeerConnected(WifiP2pInfo wifiP2pInfo) {
        MeshLog.v("OnPeerConnected...");
        GroupDevice groupDevice = wiFiP2PInstance.getThisDevice();
        if (groupDevice == null) return;

        if (groupDevice != null) {
            meshCallback.onDeviceInfoUpdated(groupDevice.getDeviceName(), wifiP2pInfo.isGroupOwner);
        } else {
            meshCallback.onDeviceInfoUpdated(wifiP2pInfo.groupOwnerAddress.getHostName(), wifiP2pInfo.isGroupOwner);
        }

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            MeshLog.v("Group owner, I'am not a client!");
            return;
        }

        if (serviceConnectedListener != null) {
            serviceConnectedListener.onServiceConnected(groupDevice);
        }

        MeshLog.v("Send own info");
        groupDevice.setDeviceServerSocketIP(wifiP2pInfo.groupOwnerAddress.getHostAddress());
        clientsConnected.put(groupDevice.getDeviceMac(), groupDevice);
        sendMyUserInfo(groupDevice.getDeviceServerSocketIP(), Constant.TYPE_USER_INFO_REQ);

    }


    private void sendMyUserInfo(String ipAddress, int type) {
        MeshLog.v("Send my info");
        GroupDevice groupDevice = wiFiP2PInstance.getThisDevice();
        String myUserInfo = JsonParser.buildMyUserInfo(groupDevice, mCurrentUser, type);

        if (myUserInfo == null) return;
        MeshLog.v("Send my info to api");
        messageSender.sendMessage(myUserInfo, ipAddress);
    }

    private void sendOtherConnectedUsersInfo(String receiverIp) {
        List<User> userList = getConnectedUsers();
        if (userList.size() == 0) return;

        String usersJson = JsonParser.buildUsersJson(userList);
        messageSender.sendMessage(usersJson, receiverIp);
    }

    private void sendNewUserToOtherUsers(User user) {
        List<User> users = new ArrayList<>();
        users.add(user);

        List<User> userList = getConnectedUsers();
        if (userList.size() == 0) return;

        String usersJson = JsonParser.buildUsersJson(users);
        MeshLog.v("Called new user broadcast to others");
        for (User item : userList) {
            messageSender.sendMessage(usersJson, item.getDeviceServerSocketIP());
        }

    }

    @Override
    public void onMessageReceived(String msg, String ipAddress) {
        int type = JsonParser.getType(msg);

        switch (type) {
            case Constant.TYPE_DEVICE_INFO:
                GroupDevice groupDevice = JsonParser.parseDeviceInfo(msg, ipAddress);
                if (groupDevice == null) return;
                clientsConnected.put(groupDevice.getDeviceMac(), groupDevice);

                //sendMyUserInfo(ipAddress);

                break;

            case Constant.TYPE_USER_INFO_REQ:
                sendMyUserInfo(ipAddress, Constant.TYPE_USER_INFO_RES);

                sendOtherConnectedUsersInfo(ipAddress);

                User user = JsonParser.parseUsers(msg, ipAddress);

                if (user == null) return;

                sendNewUserToOtherUsers(user);

                mUsersMaps.put(user.getUserId(), user);
                //call back to ui

                break;
            case Constant.TYPE_USER_INFO_RES:

                user = JsonParser.parseUsers(msg, ipAddress);
                if (user == null) return;
                mUsersMaps.put(user.getUserId(), user);
                //Call back to UI

                break;
            case Constant.TYPE_DEVICE_INFO_LIST:
                List<User> users = JsonParser.parseUsersJson(msg);
                for (User item : users) {
                    mUsersMaps.put(item.getUserId(), item);
                }
                //Callback to ui
                break;
            default:
                break;
        }
    }


}

