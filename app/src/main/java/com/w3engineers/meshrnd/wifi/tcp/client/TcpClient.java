package com.w3engineers.meshrnd.wifi.tcp.client;

import android.util.Log;

import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.wifi.ScanListener;
import com.w3engineers.meshrnd.wifi.tcp.connection.Connection;
import com.w3engineers.meshrnd.wifi.tcp.listener.ConnectionListener;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
public class TcpClient implements ConnectionListener {

    private Connection connection;
    private int serverPort;
    private ScanListener scanListener;
    private Executor tcpScanThreadExecutor;

    public TcpClient(ScanListener scanListener) {
        this.scanListener = scanListener;
        serverPort = Common.TCP_SCAN_PORT;
        tcpScanThreadExecutor = Executors.newFixedThreadPool(10);
    }

    public void establishedConnection(final String serverAddr, final String info) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket sc = null;
                try {
                    sc = new Socket(InetAddress.getByName(serverAddr), serverPort);
                    connection = new Connection(TcpClient.this, sc);
                    connection.start();
                    connection.write(info);
                    AppLog.v("Device_a rite successful  = ");

                } catch (ConnectException c) {
                    AppLog.v( "Connection Exception");
                } catch (Exception e) {
                    try {
                        if (sc != null)
                            sc.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                    AppLog.v("Device_a Exception");
                }finally {
                    if(serverAddr.contains("255")){
                      scanListener.scanFinish();
                    }
                }
            }
        }).start();


    }

    public void scanFullSubnet(final String reqString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String myIP = Common.getDeviceIp();
                    AppLog.v("Device_a  IP = " + myIP);
                    if (myIP != null) {
                        String[] tokens = myIP.split("\\.");
                        String firstPart = tokens[0];
                        String secondPart = tokens[1];
                        String thirdPart = tokens[2];
                        //String myInfo = Util.getUserInfo(null, Constant.SCAN_RES);
                        String subNet = firstPart + "." + secondPart + "." + thirdPart;
                        //Util.isTcpUserScannerRunning = true;
                        for (int i = 0; i <= 255; i++) {
                            String currentPingAddr = subNet + "." + Integer.toString(i);
                            //if(!currentPingAddr.equals(myIP)) {
                            establishedConnection(currentPingAddr, reqString);
                            AppLog.v("Ping Ip address = " + currentPingAddr);
                            Thread.sleep(50);
                            //}
                        }
                    }
                    //Util.isTcpUserScannerRunning = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    //Util.isTcpUserScannerRunning = false;
                } catch (Error e) {
                    e.printStackTrace();
                    //Util.isTcpUserScannerRunning = false;
                }
            }
        }).start();


    }


    @Override
    public void read(String msg, String ip) {
        if (scanListener != null)
            scanListener.join(msg, ip);
        AppLog.v("Device_a get client =" + msg);
    }

}
