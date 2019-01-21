package com.w3engineers.meshrnd.model;

import android.os.Build;


import java.io.Serializable;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/16/2019 at 5:06 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/14/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */
public class UserModel implements Serializable {

    private String deviceName = Build.MODEL;
    private String osVersion = Build.VERSION.RELEASE;
    private String userName = "";
    private String userId = "";
    private String ip;
    private int port;
    private boolean isGroupOwner;

    private String viaIpAddress;

    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }

    public String getViaIpAddress() {
        return viaIpAddress;
    }

    public void setViaIpAddress(String viaIpAddress) {
        this.viaIpAddress = viaIpAddress;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getUserName() {
        return userName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    public String toString() {
        //String stringRep = (new Gson()).toJson(this);
        //return stringRep;
        return "";
    }

    public static UserModel fromJSON(String jsonRep) {
        //Gson gson = new Gson();
        //UserModel deviceDTO = gson.fromJson(jsonRep, UserModel.class);
        //return deviceDTO;
        return null;
    }
}
