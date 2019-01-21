package com.w3engineers.meshrnd.util;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/15/2019 at 1:36 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/15/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import com.w3engineers.meshrnd.model.Message;
import com.w3engineers.meshrnd.model.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private static String NAME_JSON_KEY = "n";
    private static String ID_JSON_KEY = "id";
    private static String IMG_JSON_KEY = "img";
    private static String MESSAGE_TYPE = "t";
    private static String KEY_TEXT_MSG = "m";
    private static String KEY_MSG_ID = "id";
    private static String KEY_USER_ARRAY = "ua";

    public static String MSG_OWNER_IP = "moip";
    public static String MSG_VIA_IP = "mvip";


    private static JSONObject jo;

    public static int getScanType(String jsonString) {
        try {
            jo = new JSONObject(jsonString);
            if (jo.has(MESSAGE_TYPE)) {
                return jo.getInt(MESSAGE_TYPE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getReqString() {
        try {
            jo = new JSONObject();
            jo.put(NAME_JSON_KEY, SharedPref.read(Constants.NAME));
            jo.put(MESSAGE_TYPE, Common.SCAN_REQ);
            jo.put(ID_JSON_KEY, SharedPref.read(Constants.USER_ID));
            return jo.toString();
        } catch (JSONException e) {
        }
        return "";
    }


    public static String getResString() {
        try {
            jo = new JSONObject();
            jo.put(NAME_JSON_KEY, SharedPref.read(Constants.NAME));
            jo.put(MESSAGE_TYPE, Common.SCAN_RES);
            jo.put(ID_JSON_KEY, SharedPref.read(Constants.USER_ID));

            return jo.toString();
        } catch (JSONException e) {
        }
        return "";
    }

    public static UserModel parseUserData(String response, String ip) {

        UserModel userModel = new UserModel();
        try {
            jo = new JSONObject(response);
            userModel.setUserName(jo.getString(NAME_JSON_KEY));
            userModel.setUserId(jo.getString(ID_JSON_KEY));
            userModel.setIp(ip);
            userModel.setViaIpAddress(ip);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userModel;
    }

    public static String buildMessage(Message value) {
        try {
            jo = new JSONObject();
            jo.put(KEY_TEXT_MSG, value.message);
            jo.put(KEY_MSG_ID, value.messageId);
            jo.put(MESSAGE_TYPE, Common.TEXT_MESSAGE);

            return jo.toString();
        } catch (JSONException e) {
        }
        return "";
    }

    public static Message parseMessage(String jsonMsg) {
        Message message = new Message();
        try {
            jo = new JSONObject(jsonMsg);
            message.message = jo.getString(KEY_TEXT_MSG);
            message.messageId = jo.getString(KEY_MSG_ID);
            message.incoming = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;
    }

    public static String buildUserListJson(List<UserModel> userModels){
        try {
            jo = new JSONObject();
            JSONArray array = new JSONArray();

            for (UserModel item : userModels){

              JSONObject jsonObject = new JSONObject();
                jsonObject.put(NAME_JSON_KEY, item.getUserName());
                jsonObject.put(ID_JSON_KEY, item.getUserId());
                jsonObject.put(MSG_OWNER_IP, item.getIp());
                array.put(jsonObject);
            }

            jo.put(MESSAGE_TYPE, Common.TYPE_USER_LIST);
            jo.put(KEY_USER_ARRAY, array);

            return jo.toString();
        } catch (JSONException e) {
        }
        return "";

    }

    public static List<UserModel> parseJsonArray(String jsonString, String  senderIp){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(KEY_USER_ARRAY);
            List<UserModel> userModels = new ArrayList<>();
            for(int i = 0; i < jsonArray.length(); i++){
                jo = jsonArray.getJSONObject(i);
                UserModel userModel = new UserModel();

                userModel.setUserName(jo.getString(NAME_JSON_KEY));
                userModel.setUserId(jo.getString(ID_JSON_KEY));
                userModel.setIp(jo.getString(MSG_OWNER_IP));
                userModel.setViaIpAddress(senderIp);
                userModels.add(userModel);

            }
            return userModels;

        }catch (JSONException e){}

        return null;
    }
}
