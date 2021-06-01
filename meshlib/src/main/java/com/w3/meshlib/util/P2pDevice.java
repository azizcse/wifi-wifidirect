package com.w3.meshlib.util;

/**
 * Created by Azizul Islam on 6/1/21.
 */
public class P2pDevice {
    private String ssid;
    private String password;
    private String mac;

    public P2pDevice(String ssid, String password, String mac) {
        this.ssid = ssid;
        this.password = password;
        this.mac = mac;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
