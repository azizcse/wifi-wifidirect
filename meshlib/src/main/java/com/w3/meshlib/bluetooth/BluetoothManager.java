package com.w3.meshlib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.w3.meshlib.common.Constant;
import com.w3.meshlib.common.HandlerUtil;
import com.w3.meshlib.common.MeshLog;
import com.w3.meshlib.common.listeners.MeshCallback;
import com.w3.meshlib.common.listeners.MeshDataListener;
import com.w3.meshlib.data.DBManager;
import com.w3.meshlib.data.SharedPref;
import com.w3.meshlib.model.User;
import com.w3.meshlib.parser.JsonParser;
import com.w3.meshlib.service.WiFiDirectService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BluetoothManager {
    private static BluetoothManager bluetoothManager;
    private static BleListener bleListener;
    private BluetoothServer bluetoothServer;
    private static BluetoothClient bluetoothClient;

    private static Map<String, BleLink> deviceMacAndBleLinkMap = new HashMap<>();
    private static Map<String, BluetoothDevice> discoveredDeviceMap = new HashMap<>();

    private static Map<String, User> bluetoothConnectedUserMap = new HashMap<>();

    private static MeshCallback bleMeshListener;
    private static BluetoothAdapter bluetoothAdapter;
    private static Context context;

    private static MeshDataListener bleMessageListener;
    private static String MY_ID = "";
    private static String MY_MAC = "";
    private boolean isBluetoothMaster;
    private static boolean isClientConnected;
    private static Map<String, Boolean> clientAddressConnectionStateMap = new HashMap<>();

    private BluetoothManager(Context context, User user) {

        MY_MAC = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        String myInfo = JsonParser.getMyInfoJson(user);
        isBluetoothMaster = SharedPref.on(context).readBoolean(Constant.PREFKEY_BLUETOOTH_ENABLE);

        bluetoothClient = new BluetoothClient(myInfo, bleListener, MY_MAC);
        bluetoothServer = new BluetoothServer(myInfo, bleListener);
        BluetoothManager.context = context;
        //connectionListener();
        this.MY_ID = user.getUserId();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        onBluetooth();
        bluetoothAdapter.setName(Constant.BLE_MASTER_PREFIX + user.getUserName());

        registerReceiver();

        startDeviceSearching();

        if (isBluetoothMaster) {
            MeshLog.v("Bluetooth master");
        } else {
            MeshLog.v("Bluetooth client =" + MY_MAC);
        }
    }

    private void onBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            MeshLog.d("Bluetooth is Enabled");
        }
    }


    public void makeDiscoverable(Context context) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }

    public void initMeshCallback(MeshCallback listener) {
        this.bleMeshListener = listener;
    }

    public static BluetoothManager on(Context context, User userName) {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager(context, userName);
        }
        return bluetoothManager;
    }

    public static BluetoothManager getInstance() {
        return bluetoothManager;
    }

    /*public void connectionListener() {
        if (bluetoothServer != null) {
            bluetoothServer.starListenThread();
        }
    }*/


    public static List<User> getBlueToothUsers() {
        List<User> userList = new ArrayList<>();
        List<User> allList = new ArrayList<>(bluetoothConnectedUserMap.values());
        for (User item : allList) {
            if (item.getUserId() != null) {
                userList.add(item);
            }
        }
        return userList;
    }

    public void startDeviceSearching() {
        bluetoothAdapter.startDiscovery();
    }


    public void stopBle() {
        destroyAllConnection();
        stopServer();
        unregisterReceiver();
    }

    private void destroyAllConnection() {
        List<User> userList = getBlueToothUsers();
        for (User item : userList) {
            if (item.getBleLink() != null) {
                item.getBleLink().close();
            }
        }
    }


    private void stopServer() {
        if (bluetoothServer != null) {
            bluetoothServer.stopListenThread();
        }
    }

    public void sendMessage(String msg, BleLink link) {
        link.write(msg.getBytes());
    }


    public void sendMessage(String message, String userId) {
        List<User> users = getBlueToothUsers();
        //MeshLog.v("check send to Ble");
        for (User item : users) {
            if (item.getUserId().equals(userId)) {
                if (item.getBleLink() != null) {
                    MeshLog.v("Send to Ble user id=" + item.getUserId() + " ip=" + item.getIpAddress() + " mac=" + item.getDeviceMac());
                    item.getBleLink().write(message.getBytes());
                    break;
                }
            }
        }
    }


    private void unregisterReceiver() {
        MeshLog.v("Unregister BLE receiver");
        context.unregisterReceiver(myReceiver);
    }

    private void registerReceiver() {
        MeshLog.v("Register BLE receiver");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(myReceiver, intentFilter);
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            bluetoothAdapter.startDiscovery();
            try {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    //MeshLog.v("Device Found Name: " + device.getName());

                    if (device.getName() != null && device.getName().contains(Constant.BLE_MASTER_PREFIX)) {


                        if (discoveredDeviceMap.containsKey(device.getAddress())) {
                            return;
                        }


                        MeshLog.v("New device found=" + device.getAddress() + " Name =" + device.getName());

                        discoveredDeviceMap.put(device.getAddress(), device);

                        clientAddressConnectionStateMap.put(device.getAddress(), false);


                        if (isBluetoothMaster) {
                            MeshLog.v("Create master");
                            bluetoothServer.createChannelForSpecificClient(device.getAddress());

                        } else if (!isClientConnected) {
                            MeshLog.v("Create connection");
                            //bluetoothClient.createConnection(device, bluetoothAdapter);
                            startConnector();
                            User bluetoothUser = new User();
                            bluetoothUser.setDeviceName(device.getName());
                            bluetoothUser.setDeviceMac(device.getAddress());
                            bluetoothUser.setBleUser(true);

                            bluetoothConnectedUserMap.put(device.getAddress(), bluetoothUser);
                        }

                    }
                }

            } catch (Exception e) {

            }

        }
    };

    private static Queue<BluetoothDevice> bluetoothDeviceQueue = new LinkedList<>();

    private static void startConnector() {
        HandlerUtil.postBackground(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Boolean> item : clientAddressConnectionStateMap.entrySet()) {

                    if (item.getValue() == false) {
                        bluetoothDeviceQueue.add(discoveredDeviceMap.get(item.getKey()));
                    }
                }

                if (bluetoothDeviceQueue.size() > 0) {
                    makeClientConnection();
                }
            }
        }, 3000);

    }

    private static void makeClientConnection() {
        if (bluetoothDeviceQueue.size() > 0) {
            MeshLog.v("Called makeClientConnection()");
            HandlerUtil.postBackground(new Runnable() {
                @Override
                public void run() {
                    if (bluetoothDeviceQueue.size() > 0) {
                        final BluetoothDevice device = bluetoothDeviceQueue.remove();
                        if (!isClientConnected && device != null) {
                            bluetoothClient.createConnection(device, bluetoothAdapter);

                        }
                    }
                }
            }, 1000);
        } else {
            startConnector();
        }

    }


    static {
        bleListener = new BleListener() {
            @Override
            public void onDeviceConnected(String deviceAddress, BleLink link) {
                MeshLog.v("Socket connected address =" + link.getName());
                //deviceMacAndBleLinkMap.put(deviceAddress, link);
                //setConnectedLink(deviceAddress, link);
                //bluetoothAdapter.startDiscovery();
                isClientConnected = true;
                clientAddressConnectionStateMap.put(deviceAddress, true);

            }

            @Override
            public void onErrorOccurred(String errMsg, String deviceAddress) {
                MeshLog.v(errMsg);
                bluetoothAdapter.startDiscovery();
                makeClientConnection();

            }

            @Override
            public void onConnectionLost(String mac) {

                MeshLog.v("Connection closed and remove from discovery");
                if (MY_MAC.equals(mac)) {
                    discoveredDeviceMap.remove(mac);
                    clientAddressConnectionStateMap.put(mac, false);
                }
                bluetoothAdapter.startDiscovery();


            }

            @Override
            public void onReceivedMessage(BleLink link, String message) {
                MeshLog.v("Message received from =" + link.getName());
                int type = JsonParser.getType(message);


                switch (type) {
                    case Constant.TYPE_BLE_HELLO:



                        User bluetoothUser = JsonParser.parseUserInfoJson(message);

                        bluetoothUser.setBleLink(link);
                        bluetoothUser.setBleUser(true);
                        bluetoothUser.setDirectConnection(true);
                        bleMeshListener.onUserDiscovered(bluetoothUser);
                        sendNewUsersToWiFiUsers(bluetoothUser);

                        setUserInDb(bluetoothUser);
                        sendWiFiUsersToCurrentBleUser(link);
                        sendCurrentUserToOtherBleUsers(bluetoothUser);
                        bluetoothConnectedUserMap.put(bluetoothUser.getDeviceMac(), bluetoothUser);


                        /*boolean isCreateClientSocket = findUserModel(message, link);

                        sendWiFiUsersToCurrentBleUser(link);

                        if (!isCreateClientSocket) {
                            String friendsMac = JsonParser.getFriendsMac(message);
                            if (TextUtils.isEmpty(friendsMac)) return;

                            User bluetoothUser = JsonParser.parseUserInfoJson(message);

                            User bleUser = bluetoothConnectedUserMap.get(friendsMac);

                            if (bleUser == null) {

                                bluetoothUser.setBleLink(link);
                                bluetoothUser.setBleUser(true);
                                bluetoothUser.setDirectConnection(true);
                                bleMeshListener.onUserDiscovered(bluetoothUser);
                                sendNewUsersToWiFiUsers(bluetoothUser);

                                setUserInDb(bluetoothUser);

                                bluetoothConnectedUserMap.put(bluetoothUser.getDeviceMac(), bluetoothUser);

                            } else {

                                bleUser.setUserId(bluetoothUser.getUserId());
                                bleUser.setBleLink(link);
                                bleUser.setUserName(bluetoothUser.getUserName());
                                bleUser.setBleUser(true);
                                bleUser.setDirectConnection(true);
                                bleMeshListener.onUserDiscovered(bleUser);
                                sendNewUsersToWiFiUsers(bleUser);

                                setUserInDb(bleUser);
                            }
                        }*/


                        break;
                    case Constant.TYPE_USER_DISCO:
                        MeshLog.v("Received TYPE_USER_DISCO");

                        User user = JsonParser.parseBleSingleUserJson(message, link);
                        if (!bluetoothConnectedUserMap.containsKey(user.getDeviceMac())) {
                            sendNewUsersToWiFiUsers(user);
                        }

                        bluetoothConnectedUserMap.put(user.getDeviceMac(), user);

                        bleMeshListener.onUserDiscovered(user);

                        setUserInDb(user);

                        break;
                    case Constant.TYPE_TEXT_MESSAGE:
                        String receiverId = JsonParser.getReceiver(message);

                        MeshLog.v("TYPE_TEXT_MESSAGE received");
                        if (MY_ID.equals(receiverId)) {
                            if (bleMessageListener != null) {
                                bleMessageListener.onDataReceived(message);
                            }
                        } else {

                            WiFiDirectService.on().sendMessageBasedOnRoutingInfo(message, receiverId);
                        }

                        if (bleMeshListener != null) {
                            bleMeshListener.onMessageReceived(message, link.getName());
                        }
                        break;
                }
            }

        };
    }


    private static void setConnectedLink(String address, BleLink link) {
        User bluetoothUser = bluetoothConnectedUserMap.get(address);
        bluetoothUser.setBleLink(link);

    }


    private static boolean findUserModel(String msg, BleLink bleLink) {
        User bluetoothUser = JsonParser.parseUserInfoJson(msg);

        String linkId = bleLink.getName();

        Iterator hmIterator = deviceMacAndBleLinkMap.entrySet().iterator();
        MeshLog.v("User info parsed");

        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            BleLink link = (BleLink) mapElement.getValue();

            if (link.getName().equals(linkId)) {

                String deviceId = (String) mapElement.getKey();
                User bleUser = bluetoothConnectedUserMap.get(deviceId);

                bleUser.setUserName(bluetoothUser.getUserName());
                bleUser.setUserId(bluetoothUser.getUserId());
                bleUser.setDirectConnection(true);
                MeshLog.v("User info send to UI");
                bleMeshListener.onUserDiscovered(bleUser);
                sendNewUsersToWiFiUsers(bleUser);

                setUserInDb(bleUser);
                return true;
            }
        }

        return false;

    }

    public void initMessageReceiver(MeshDataListener bleMessage) {
        this.bleMessageListener = bleMessage;
    }


    private static void sendNewUsersToWiFiUsers(User user) {
        WiFiDirectService.on().sendBleDiscoveredUsersToWiFi(user);
    }

    private static void sendWiFiUsersToCurrentBleUser(BleLink bleLink) {
        List<User> userList = WiFiDirectService.on().getConnectedUsers();

        for (User item : userList) {
            String userJson = JsonParser.buildUserJsonToSendDirectConnectedUser(item, Constant.USER_TYPE_VIA_ME);
            if (TextUtils.isEmpty(userJson)) continue;
            bleLink.write(userJson.getBytes());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<User> bleUserList = getBlueToothUsers();
        for (User item : bleUserList) {
            String userJson = JsonParser.buildUserJsonToSendDirectConnectedUser(item, Constant.USER_TYPE_VIA_ME);
            bleLink.write(userJson.getBytes());
        }
    }

    private static void sendCurrentUserToOtherBleUsers(User user){
        List<User> bleUserList = getBlueToothUsers();
        for (User item : bleUserList) {
            String userJson = JsonParser.buildUserJsonToSendDirectConnectedUser(user, Constant.USER_TYPE_VIA_ME);
            if(item.getBleLink() != null) {
                item.getBleLink().write(userJson.getBytes());
            }
        }
    }

    public void sendP2pUsersToBleUsers(User user) {
        List<User> userList = getBlueToothUsers();
        String userJson = JsonParser.buildUserJsonToSendDirectConnectedUser(user, Constant.USER_TYPE_VIA_ME);
        for (User item : userList) {
            if (item.isDirectConnection()) {
                item.getBleLink().write(userJson.getBytes());
            }
        }
    }

    private static void setUserInDb(User user) {
        DBManager.on().saveUser(user);
    }


}
