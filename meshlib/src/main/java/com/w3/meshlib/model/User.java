package com.w3.meshlib.model;

import com.w3.meshlib.common.GroupDevice;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class User extends GroupDevice {
    @Id
    public long id;
    private String userName;
    private String userId;

    public String getUserName() {
        return userName;
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
        return "{name ="+userName+" Id="+id+" Device anme="+getDeviceName()+" Mac="+getDeviceMac()+" Ip="+getDeviceServerSocketIP()+"}";
    }
}
