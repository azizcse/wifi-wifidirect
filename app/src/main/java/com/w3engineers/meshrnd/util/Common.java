package com.w3engineers.meshrnd.util;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/15/2019 at 11:17 AM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/15/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.content.Context;
import android.net.wifi.WifiManager;

import com.w3.meshlib.data.SharedPref;
import com.w3.meshlib.model.User;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class Common {
    public static int MESSAGING_PORT_NUMBER = 1062;
    public static int TCP_SCAN_PORT = 1065;
    public static int UDP_IP_SCANNER_PORT = 1066;
    public static final String INITIAL_IP = "0.0.0.0";
    public static final int SCAN_BUF = 20480;
    public static final int MESSAGING_PORT = 2049;

    public static final int SCAN_REQ = 0;
    public static final int SCAN_RES = 1;

    public static final int TEXT_MESSAGE = 100;

    public static final int TYPE_USER_LIST = 3;

    public static final int UDP_SCAN_NUMBER = 2;


    public synchronized static void closeSocket(Socket socket) {
        while (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public static String getDeviceIp() {
        String deviceIp = null;
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        deviceIp = inetAddress.getHostAddress();
                    }
                }
            }
            AppLog.e(" Device ip = " + deviceIp);
            return deviceIp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method find out Wifi or My device IP address
     * Calculate which sub net the user need to be searched.
     * Generate a modified broadcast IP address.
     *
     * @return commonBroadcastIpAddress(String) Broadcast IP address.
     */
    public static String getHotspotBroadcastIpAddress(Context context){
        boolean isHotspot = isHotspotOpen(context);
        String commonBroadcastIpAddress = null;
        String myIPAddress = null;
        if (isHotspot) {
            myIPAddress = getWifiApIpAddress();
        } else {
            myIPAddress = getDeviceIp();
        }

        //Log.e("IPAddr", "Ip TO_ADDRESS: "+myIPAddress+", Hotspot: "+isHotspot);

        if (myIPAddress != null) {
            //String myIP = IPTools.getDeviceIpAddress();
            String[] tokens = myIPAddress.split("\\.");

            String firstPart = tokens[0];
            String secondPart = tokens[1];
            String thirdPart = tokens[2];
            String fourthPart = tokens[3];

            String upperLimit = "255";
            commonBroadcastIpAddress = firstPart + "." + secondPart + "." + thirdPart + "." + upperLimit;
        }
        return commonBroadcastIpAddress;
    }

    /**
     * Simply return UDP Broadcast IP TO_ADDRESS
     * @return
     */
    public static String getWifiBroadcastIpAddress(){
        String wifiBroadcastIpAddress = "255.255.255.255";
        return wifiBroadcastIpAddress;
    }

    public static boolean isHotspotOpen(Context _nContext) {
        boolean isHotspot = false;
        try {
            WifiManager wifiManager = (WifiManager) _nContext
                    .getSystemService(Context.WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod(
                    "getWifiApState");
            method.setAccessible(true);
            int actualState = (Integer) method.invoke(wifiManager,
                    (Object[]) null);
            // public static int AP_STATE_DISABLING = 10;
            // public static int AP_STATE_DISABLED = 11;
            int AP_STATE_ENABLING = 12;
            int AP_STATE_ENABLED = 13;
            // public static int AP_STATE_FAILED = 14;
            isHotspot = actualState == AP_STATE_ENABLING || actualState == AP_STATE_ENABLED;
        } catch (Exception e) {
            isHotspot = false;
        }
        return isHotspot;
    }

    public static String getWifiApIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.isLoopback()) {
                    continue;
                }
                if (intf.isVirtual()) {
                    continue;
                }
                if (!intf.isUp()) {
                    continue;
                }
                if (intf.isPointToPoint()) {
                    continue;
                }
                if (intf.getHardwareAddress() == null) {
                    continue;
                }
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress.getAddress().length == 4) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getMyInfo() {
        User user = new User();
        user.setUserName(SharedPref.read(Constants.NAME));
        user.setUserId(SharedPref.read(Constants.USER_ID));
        return user;
    }


    public static User currentChatUser = null;

}
