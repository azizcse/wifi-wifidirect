package com.w3.meshlib.model;

import android.support.annotation.IntDef;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Routing {
    @Id
    private long id;
    private String userId;
    private String ipAddress;
    private String macAddress;
    private boolean isBleUser;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isBleUser() {
        return isBleUser;
    }

    public void setBleUser(boolean bleUser) {
        isBleUser = bleUser;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMedium() {
        return ipAddress;
    }

    public void setMedium(String medium) {
        this.ipAddress = medium;
    }


    @Override
    public String toString() {
        return "Routing info [ id ="+userId+" ip="+ipAddress+" mac="+macAddress+ " is_ble ="+isBleUser+" ]";
    }
}
