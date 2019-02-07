package com.w3.meshlib.common;

import java.util.ArrayList;
import java.util.UUID;

public class Constant {
    public static final String BLE_MASTER_PREFIX = "mesh-";
    public static final String BLE_CLIENT_PREFIX = "client-";

    public static final String BLE_CHANNEL_PREFIX = "f520cf2c-6487-11e7-907b-";

    public static final String NAME_SECURE = "BluetoothChatSecure";
    public static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    public static final UUID MY_UUID_SECURE = UUID
            .fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static final int MESSAGING_PORT = 1067;

    public static final int TYPE_DEVICE_INFO = 0;
    public static final int TYPE_DEVICE_INFO_LIST = 1;
    public static final int TYPE_USER_HELLO_REQ = 2;
    public static final int TYPE_USER_HELLO_RES = 3;
    public static final int TYPE_SINGLE_USER_INFO_RES = 5;
    public static final int TYPE_BLE_HELLO = 4;
    public static final int TYPE_USER_DISCO = 6;




    public static final int TYPE_TEXT_MESSAGE = 100;


    public static String KEY_DEVICE_NAME = "dn";
    public static String KEY_DEVICE_MAC = "dm";
    public static String KEY_MSG_TYPE = "t";
    public static String KEY_USER_NAME = "un";
    public static String KEY_USER_ID = "uid";
    public static String KEY_USER_DATA = "ud";
    public static String KEY_USER_IP = "uip";
    public static String KEY_FRIENDS_MAC = "mac";

    public static String KEY_RECEIVER_ID = "rid";
    public static String KEY_SENDER_ID = "sid";

    public static String KEY_MEDIUM = "media";


    public static String KEY_USER_TYPE = "ut";
    public static final int USER_TYPE_P2P = 200;
    public static final int USER_TYPE_BLE = 300;

    public static String PREFKEY_MASTER_ENABLE = "master";
    public static String PREFKEY_BLUETOOTH_ENABLE = "bluetooth";


    private static ArrayList<UUID> mUuids;

    public static ArrayList<UUID> generateUUIDs(){

        if(mUuids == null){

            mUuids = new ArrayList<>();

            // generate unique uuids for the session
            mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
            mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
            mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
            mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
            mUuids.add(UUID.fromString("a81d6504-45a3-44ee-a477-7d97d09469e4"));
            mUuids.add(UUID.fromString("a81d6504-4b3f-48fe-a476-7d98d09459e4"));
            mUuids.add(UUID.fromString("a81d6504-4d3c-43ee-a485-7d99d09449e4"));
        }
        return mUuids;
    }

    /****************From WHERE******************/
    public static final int USER_TYPE_DIRECT = 300;
    public static final int USER_TYPE_VIA_ME= 301;


}
