package com.w3.meshlib.model;

import com.w3.meshlib.bluetooth.BleLink;
import com.w3.meshlib.common.GroupDevice;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class User {
    @Id
    public long id;
    private String userName;
    private String userId;
    private String deviceName;
    private String deviceMac;
    private String ipAddress;
    private boolean isDirectConnection;

    @Transient
    private BleLink bleLink;

    private boolean bleUser;

    public boolean isDirectConnection() {
        return isDirectConnection;
    }

    public void setDirectConnection(boolean directConnection) {
        isDirectConnection = directConnection;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserName() {
        return userName;
    }

    public BleLink getBleLink() {
        return bleLink;
    }

    public boolean isBleUser() {
        return bleUser;
    }

    public void setBleUser(boolean bleUser) {
        this.bleUser = bleUser;
    }

    public void setBleLink(BleLink bleLink) {
        this.bleLink = bleLink;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "{name ="+userName+" Id="+id+" Device anme="+getDeviceName()+" Mac="+getDeviceMac()+" Ip="+ getIpAddress()+"}";
    }
}
