package com.w3.meshlib.parser;

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
    public static String buildMyDeviceInfo(GroupDevice groupDevice) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constant.KEY_DEVICE_NAME, groupDevice.getDeviceName());
            jsonObject.put(Constant.KEY_DEVICE_MAC, groupDevice.getDeviceMac());
            jsonObject.put(Constant.KEY_MSG_TYPE, Constant.TYPE_DEVICE_INFO);
            return jsonObject.toString();
        } catch (JSONException e) {
        }
        return null;
    }

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
            groupDevice.setDeviceServerSocketIP(ipAddress);
            return groupDevice;
        } catch (JSONException e) {
        }
        return null;
    }

    public static String buildMyUserInfo(GroupDevice groupDevice, User mCurrentUser, int type) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put(Constant.KEY_USER_NAME, mCurrentUser.getUserName());
            jsonObject.put(Constant.KEY_USER_ID, mCurrentUser.getUserId());

            jsonObject.put(Constant.KEY_DEVICE_NAME, groupDevice.getDeviceName());
            jsonObject.put(Constant.KEY_DEVICE_MAC, groupDevice.getDeviceMac());
            jsonObject.put(Constant.KEY_MSG_TYPE, type);
            MeshLog.v("My Info Before send ="+jsonObject.toString());
            return jsonObject.toString();
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
            jsonObject.put(Constant.KEY_USER_IP, mCurrentUser.getDeviceServerSocketIP());

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
            user.setDeviceServerSocketIP(ip);
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
            user.setDeviceServerSocketIP(jsonObject.getString(Constant.KEY_USER_IP));
            return user;
        } catch (JSONException e) {
        }

        return null;
    }
    public static String buildUsersJson(List<User> userList) {
        try {
            JSONObject jo = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (User item : userList) {
                JSONObject jsonObject = buildUserInfoJson(item);
                jsonArray.put(jsonObject);
            }
            jo.put(Constant.KEY_MSG_TYPE, Constant.TYPE_DEVICE_INFO_LIST);
            jo.put(Constant.KEY_USER_DATA, jsonArray);
            jo.toString();
        } catch (JSONException e) {
        }
        return null;
    }

    public static List<User> parseUsersJson(String usersJson){
        List<User> users = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(usersJson);
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.KEY_USER_DATA);

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jo = jsonArray.getJSONObject(i);
                User user = parseUsers(jo);

                if(user != null){
                    users.add(user);
                }
            }
        }catch (JSONException e){}
        return users;
    }
}
