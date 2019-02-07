package com.w3.meshlib.parser;

import com.w3.meshlib.MeshApp;
import com.w3.meshlib.bluetooth.BleLink;
import com.w3.meshlib.common.Constant;
import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.MeshLog;
import com.w3.meshlib.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {


    public static int getType(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.getInt(Constant.KEY_MSG_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static GroupDevice parseDeviceInfo(String msg, String ipAddress) {
        try {
            JSONObject jo = new JSONObject(msg);
            String name = jo.getString(Constant.KEY_DEVICE_NAME);
            String mac = jo.getString(Constant.KEY_DEVICE_MAC);
            GroupDevice groupDevice = new GroupDevice();
            groupDevice.setDeviceName(name);
            groupDevice.setDeviceMac(mac);
            groupDevice.setIpAddress(ipAddress);
            return groupDevice;
        } catch (JSONException e) {
        }
        return null;
    }


    public static JSONObject buildUserInfoJson(User mCurrentUser) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constant.KEY_USER_NAME, mCurrentUser.getUserName());
            jsonObject.put(Constant.KEY_USER_ID, mCurrentUser.getUserId());
            jsonObject.put(Constant.KEY_DEVICE_NAME, mCurrentUser.getDeviceName());
            jsonObject.put(Constant.KEY_DEVICE_MAC, mCurrentUser.getDeviceMac());
            jsonObject.put(Constant.KEY_USER_IP, mCurrentUser.getIpAddress());

            return jsonObject;
        } catch (JSONException e) {
        }

        return null;
    }

    public static User parseUsers(String msg, String ip) {
        try {
            JSONObject jsonObject = new JSONObject(msg);
            User user = new User();
            user.setUserName(jsonObject.getString(Constant.KEY_USER_NAME));
            user.setUserId(jsonObject.getString(Constant.KEY_USER_ID));
            user.setDeviceName(jsonObject.getString(Constant.KEY_DEVICE_NAME));
            user.setDeviceMac(jsonObject.getString(Constant.KEY_DEVICE_MAC));
            user.setIpAddress(ip);
            return user;
        } catch (JSONException e) {
        }

        return null;
    }

    public static User parseUsers(JSONObject jsonObject) {
        try {
            User user = new User();
            user.setUserName(jsonObject.getString(Constant.KEY_USER_NAME));
            user.setUserId(jsonObject.getString(Constant.KEY_USER_ID));
            user.setDeviceName(jsonObject.getString(Constant.KEY_DEVICE_NAME));
            user.setDeviceMac(jsonObject.getString(Constant.KEY_DEVICE_MAC));
            String ip = jsonObject.has(Constant.KEY_USER_IP) ? jsonObject.getString(Constant.KEY_USER_IP) : "";
            user.setIpAddress(ip);

            return user;
        } catch (JSONException e) {
        }

        return null;
    }

    public static String buildUsersJson(List<User> userList, List<User> bluetoothUsers) {
        try {
            JSONObject jo = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (User item : userList) {
                JSONObject jsonObject = buildUserInfoJson(item);
                jsonObject.put(Constant.KEY_USER_TYPE, Constant.USER_TYPE_P2P);
                jsonArray.put(jsonObject);
            }

            for (User item : bluetoothUsers) {
                JSONObject jsonObject = buildUserInfoJson(item);
                jsonObject.put(Constant.KEY_USER_TYPE, Constant.USER_TYPE_BLE);
                jsonArray.put(jsonObject);
            }

            jo.put(Constant.KEY_MSG_TYPE, Constant.TYPE_DEVICE_INFO_LIST);
            jo.put(Constant.KEY_USER_DATA, jsonArray);

            return jo.toString();
        } catch (JSONException e) {

        }
        return null;
    }


    public static String buildSingleUserRes(User user) {
        try {
            JSONObject jo = buildUserInfoJson(user);

            jo.put(Constant.KEY_USER_TYPE, Constant.USER_TYPE_P2P);

            jo.put(Constant.KEY_MSG_TYPE, Constant.TYPE_SINGLE_USER_INFO_RES);

            return jo.toString();

        } catch (JSONException e) {

        }
        return "";
    }


    public static List<User> parseUsersJson(String usersJson, String ipAddress) {
        List<User> users = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(usersJson);
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.KEY_USER_DATA);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                User user = parseUsers(jo);

                if (user == null) {
                    MeshLog.v("Parsing user NULL");
                    continue;
                }

                int type = jo.getInt(Constant.KEY_USER_TYPE);
                if (type == Constant.USER_TYPE_BLE) {
                    user.setBleUser(true);
                    user.setIpAddress(ipAddress);
                } else {
                    user.setBleUser(false);
                }

                if (user != null) {
                    users.add(user);
                }
            }
        } catch (JSONException e) {
        }
        return users;
    }

    public static List<User> parseUsersJson(String usersJson, BleLink ipAddress) {
        List<User> users = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(usersJson);
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.KEY_USER_DATA);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                User user = parseUsers(jo);

                if (user == null) {
                    MeshLog.v("Parsing user NULL");
                    continue;
                }

                int type = jo.getInt(Constant.KEY_USER_TYPE);
                if (type == Constant.USER_TYPE_BLE) {
                    user.setBleUser(true);

                } else {
                    user.setBleUser(false);
                }
                user.setBleLink(ipAddress);

                if (user != null) {
                    users.add(user);
                }
            }
        } catch (JSONException e) {
        }
        return users;
    }


    public static String getMyInfoJson(User user) {
        try {
            String macAddress = android.provider.Settings.Secure.getString(MeshApp.getContext().getContentResolver(), "bluetooth_address");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constant.KEY_USER_NAME, user.getUserName());
            jsonObject.put(Constant.KEY_USER_ID, user.getUserId());
            jsonObject.put(Constant.KEY_FRIENDS_MAC, macAddress);
            jsonObject.put(Constant.KEY_MSG_TYPE, Constant.TYPE_BLE_HELLO);
            return jsonObject.toString();
        } catch (JSONException e) {
        }
        return null;
    }

    public static User parseUserInfoJson(String user) {
        try {
            JSONObject jsonObject = new JSONObject(user);
            User bluetoothUser = new User();
            bluetoothUser.setUserName(jsonObject.getString(Constant.KEY_USER_NAME));
            bluetoothUser.setUserId(jsonObject.getString(Constant.KEY_USER_ID));
            bluetoothUser.setDeviceMac(jsonObject.getString(Constant.KEY_FRIENDS_MAC));
            bluetoothUser.setDirectConnection(true);
            return bluetoothUser;
        } catch (JSONException e) {
        }
        return null;
    }

    public static String getFriendsMac(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            return jsonObject.getString(Constant.KEY_FRIENDS_MAC);
        } catch (JSONException e) {
        }

        return null;
    }

    public static int getUserType(String msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg);

            return jsonObject.getInt(Constant.KEY_USER_TYPE);
        } catch (JSONException e) {
        }
        return -1;
    }

    public static String getReceiver(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            return jsonObject.getString(Constant.KEY_RECEIVER_ID);
        } catch (JSONException e) {
        }
        return "";
    }


    /*****************New Process******************/

    /**
     * Hello message builder
     *
     * @param groupDevice  : GroupDevice
     * @param mCurrentUser : User
     * @param type         : int
     * @return : String
     */
    public static String buildMyHelloInfo(GroupDevice groupDevice, User mCurrentUser, int type) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put(Constant.KEY_USER_NAME, mCurrentUser.getUserName());
            jsonObject.put(Constant.KEY_USER_ID, mCurrentUser.getUserId());

            jsonObject.put(Constant.KEY_DEVICE_NAME, groupDevice.getDeviceName());
            jsonObject.put(Constant.KEY_DEVICE_MAC, groupDevice.getDeviceMac());
            jsonObject.put(Constant.KEY_MSG_TYPE, type);

            return jsonObject.toString();
        } catch (JSONException e) {
        }

        return null;
    }

    public static String buildUserJsonToSendDirectConnectedUser(User user, int medium) {
        JSONObject jo = new JSONObject();
        try {

            jo.put(Constant.KEY_USER_NAME, user.getUserName());
            jo.put(Constant.KEY_USER_ID, user.getUserId());
            jo.put(Constant.KEY_DEVICE_MAC, user.getDeviceMac());
            jo.put(Constant.KEY_USER_IP, user.getIpAddress());
            jo.put(Constant.KEY_MSG_TYPE, Constant.TYPE_USER_DISCO);
            jo.put(Constant.KEY_MEDIUM, medium);
        } catch (JSONException e) {
        }
        return jo.toString();
    }


    public static User parseP2pSingleUserJson(String userJson, String ipAddress) {
        User user = new User();
        try {

            JSONObject jsonObject = new JSONObject(userJson);
            String name = jsonObject.getString(Constant.KEY_USER_NAME);
            String userId = jsonObject.getString(Constant.KEY_USER_ID);
            int media = jsonObject.getInt(Constant.KEY_MEDIUM);

            String mac = jsonObject.has(Constant.KEY_DEVICE_MAC) ? jsonObject.getString(Constant.KEY_DEVICE_MAC) : "";
            String ip = jsonObject.has(Constant.KEY_USER_IP) ? jsonObject.getString(Constant.KEY_USER_IP) : "";

            if (media == Constant.USER_TYPE_VIA_ME) {
                user.setIpAddress(ipAddress);
                user.setDirectConnection(false);
            } else {
                user.setDirectConnection(true);
                user.setIpAddress(ip);
            }

            user.setUserName(name);
            user.setDeviceMac(mac);
            user.setUserId(userId);
            user.setBleUser(false);

        } catch (JSONException e) {
        }
        return user;
    }

    public static User parseBleSingleUserJson(String userJson, BleLink bleLink) {
        User user = new User();
        try {

            JSONObject jsonObject = new JSONObject(userJson);
            String name = jsonObject.getString(Constant.KEY_USER_NAME);
            String userId = jsonObject.getString(Constant.KEY_USER_ID);
            int media = jsonObject.getInt(Constant.KEY_MEDIUM);

            String mac = jsonObject.has(Constant.KEY_DEVICE_MAC) ? jsonObject.getString(Constant.KEY_DEVICE_MAC) : "";
            String ip = jsonObject.has(Constant.KEY_USER_IP) ? jsonObject.getString(Constant.KEY_USER_IP) : "";

            if (media == Constant.USER_TYPE_VIA_ME) {
                user.setDirectConnection(false);
            } else {
                user.setDirectConnection(true);
            }
            user.setBleLink(bleLink);
            user.setUserName(name);
            user.setDeviceMac(mac);
            user.setUserId(userId);
            user.setIpAddress(ip);
            user.setBleUser(true);

        } catch (JSONException e) {
        }
        return user;
    }
}
