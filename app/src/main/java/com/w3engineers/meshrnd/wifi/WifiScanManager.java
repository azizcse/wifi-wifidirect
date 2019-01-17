package com.w3engineers.meshrnd.wifi;

import android.content.Context;
import android.util.Log;

import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.util.HandlerUtil;
import com.w3engineers.meshrnd.util.JsonParser;
import com.w3engineers.meshrnd.wifi.tcp.client.TcpClient;
import com.w3engineers.meshrnd.wifi.tcp.server.TcpServer;
import com.w3engineers.meshrnd.wifi.udp.UdpScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/14/2019 at 5:06 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/14/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

public class WifiScanManager {

    private static ScanListener scanListener;
    private Context context;
    private static WifiScanManager udpTcpWrapper;
    private boolean isRunning = false;
    private UdpScanner udpScanner;
    private TcpServer tcpServer;
    private TcpClient tcpClient;

    private static String reqString;
    private static String resString;

    private static List<UserModel> userModelList = new ArrayList<>();

    private static WiFiScanCallBack scanFinishListener;

    static {
        scanListener = new ScanListener() {
            @Override
            public void join(String jsonString, String ip) {
                AppLog.v("Received user info =" + jsonString + " ip" + ip);
                UserModel userModel = JsonParser.parseUserData(jsonString, ip);

                if (!isItemExist(userModel)) {
                    userModelList.add(userModel);
                    scanFinishListener.onUserFound(userModel);
                }
            }

            @Override
            public void update(String jsonString) {
                AppLog.v("Received user info =" + jsonString);
            }

            @Override
            public void scanFinish() {
                stopProgress();
            }
        };
    }

    private WifiScanManager(Context context) {
        this.context = context;
        this.reqString = JsonParser.getReqString();
        this.resString = JsonParser.getResString();
        udpScanner = new UdpScanner(context, scanListener);
        udpScanner.startSocket();

        tcpServer = new TcpServer(scanListener);
        tcpClient = new TcpClient(scanListener);
        try {
            tcpServer.startTcpSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * *********************************************
         * <p>Enable below code to perform wifi scan</p>
         * *********************************************
         */

         startServer();
    }

    public static WifiScanManager getInstance(Context context, WiFiScanCallBack listener) {
        if (udpTcpWrapper == null) {
            udpTcpWrapper = new WifiScanManager(context);
        }
        scanFinishListener = listener;
        return udpTcpWrapper;
    }

    public static WifiScanManager getInstance() {
        return udpTcpWrapper;
    }

    private void startServer() {
        if (udpScanner != null) {
            udpScanner.udpServer(resString);
        }

        if (tcpServer != null) {
            tcpServer.startServer(resString);
        }

    }

    /**
     * *********************************************
     * <p>Enable below code to perform wifi scan</p>
     * *********************************************
     */
    public void startMainScanner() {
        runScanner();
    }

    private void stopMainScanner() {
        if (udpScanner != null) {
            udpScanner.destroySocket();
        }

        if (tcpServer != null) {
            try {
                tcpServer.stopServerSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void runScanner() {
        Thread runnerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String myIP = Common.getDeviceIp();
                Log.e("UDP_A", "My ip =" + myIP);
                if (myIP == null) return;

                isRunning = true;
                for (int i = 0; i < Common.UDP_SCAN_NUMBER; i++) {
                    udpUserScan(reqString);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                tcpClient.scanFullSubnet(reqString);

                isRunning = false;
            }
        });
        runnerThread.start();
    }


    /**
     * <p>Generate wifi and hotspot scan IP addredd</p>
     *
     * @param reqString : String
     */
    private void udpUserScan(String reqString) {
        boolean isHotspot = Common.isHotspotOpen(context);
        String hotspotBroadcastIpAddress = Common.getHotspotBroadcastIpAddress(context);
        String wifiBroadcastIpAddress = Common.getWifiBroadcastIpAddress();
        AppLog.v(" Udp hots ip =" + hotspotBroadcastIpAddress);
        AppLog.v(" Udp wifi ip =" + wifiBroadcastIpAddress);
        if (hotspotBroadcastIpAddress != null && udpScanner != null) {
            udpScanner.udpClient(hotspotBroadcastIpAddress, reqString);

            if (!isHotspot) {
                udpScanner.udpClient(wifiBroadcastIpAddress, reqString);
            }
        }
    }


    private static void stopProgress() {
        HandlerUtil.postBackground(new Runnable() {
            @Override
            public void run() {
                scanFinishListener.onScanFinish();
            }
        }, 2000);
    }

    private static boolean isItemExist(UserModel userModel) {

        for (UserModel userModel1 : userModelList) {
            if (userModel1.getUserId().equals(userModel.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Return discovered user list</p>
     *
     * @return: List
     */
    public List<UserModel> getUserModelList() {
        return new ArrayList<>(userModelList);
    }
}
