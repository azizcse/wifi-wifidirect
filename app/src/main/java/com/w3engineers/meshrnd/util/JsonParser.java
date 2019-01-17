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

import com.w3engineers.meshrnd.model.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
    private static String NAME_JSON_KEY = "n";
    private static String ID_JSON_KEY = "id";
    private static String IMG_JSON_KEY = "img";
    private static String SCAN_INFO_TYPE = "t";
    private static JSONObject jo;

    public static int getScanType(String jsonString){
        try {
            jo= new JSONObject(jsonString);
            if(jo.has(SCAN_INFO_TYPE)){
                return jo.getInt(SCAN_INFO_TYPE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getReqString(){
        try {
            jo = new JSONObject();
            jo.put(NAME_JSON_KEY, SharedPref.read(Constants.NAME));
            jo.put(SCAN_INFO_TYPE, Common.SCAN_REQ);
            jo.put(ID_JSON_KEY,  SharedPref.read(Constants.USER_ID));
            return jo.toString();
        }catch (JSONException e){}
        return "";
    }


    public static String getResString(){
        try {
            jo = new JSONObject();
            jo.put(NAME_JSON_KEY, SharedPref.read(Constants.NAME));
            jo.put(SCAN_INFO_TYPE, Common.SCAN_RES);
            jo.put(ID_JSON_KEY,  SharedPref.read(Constants.USER_ID));

            return jo.toString();
        }catch (JSONException e){}
        return "";
    }

    public static UserModel parseUserData(String response, String ip){

        UserModel userModel = new UserModel();
        try {
            jo = new JSONObject(response);
            userModel.setUserName(jo.getString(NAME_JSON_KEY));
            userModel.setUserId(jo.getString(ID_JSON_KEY));
            userModel.setIp(ip);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userModel;
    }

}
