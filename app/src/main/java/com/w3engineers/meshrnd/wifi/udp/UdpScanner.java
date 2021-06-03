package com.w3engineers.meshrnd.wifi.udp;

import android.content.Context;
import android.util.Log;

import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.util.HandlerUtil;
import com.w3engineers.meshrnd.util.JsonParser;
import com.w3engineers.meshrnd.wifi.ScanListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
public class UdpScanner {
    //Final variable
    public static final String NOTIFICATION = "com.example.udp_test";
    /*
     * Instance variable
     */
    private String TAG = "UDPIpScanner";
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private String my_ip = "";
    private ArrayList<String> connectedIpList, chatIpList;
    private boolean runner = false;
    private Context context;
    //private UserParser userParser;
    private ScanListener scanListener;

    public UdpScanner(Context context, ScanListener scanListener) {
        this.scanListener = scanListener;
        this.context = context;
        //this.userParser = new UserParser();
    }

    /*
     * initialize socket component
     */

    public void startSocket() {
        // my_ip = Utils.getDeviceIp();
        runner = true;
        try {
            // InetAddress address = InetAddress.getByName("192.168.49.1");
            receiverSocket = new DatagramSocket(Common.UDP_IP_SCANNER_PORT);
            receiverSocket.setBroadcast(true);

        } catch (Exception e) {
            Log.d(TAG, "Failed to initialize socket component");
            e.printStackTrace();
        }

    }

    /*
     * Destroy socket component
     */
    public void destroySocket() {
        runner = false;
        if (receiverSocket != null) {
            receiverSocket.close();
            Log.e(TAG, "UDP socket closed");
        }

    }


    /*
     * Get new user ip address
     */
    public void udpServer(final String resString) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buf = new byte[Common.SCAN_BUF];
                    try {
                        while (runner) {
                            AppLog.v("Udp server running");
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            receiverSocket.receive(packet);

                            String ip = packet.getAddress().toString().substring(1);
                            String serverMessage = new String(packet.getData(), 0, packet.getLength());
                            AppLog.v("Udp server receive msg =" + serverMessage);
                            int type = JsonParser.getScanType(serverMessage);

                            if (Common.SCAN_REQ == type) {
                               /* byte[] sendData = resString.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                        packet.getAddress(),Common.UDP_IP_SCANNER_PORT);
                                receiverSocket.send(sendPacket);*/
                            }

                            //scanListener.join(serverMessage, ip);
                        }
                    } catch (SocketTimeoutException e) {
                        AppLog.v("Receive timed out");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Log.e(TAG, "UDP socket closing");
                    destroySocket();
                }
            }
        }).start();

    }

    public void UDPMultiCastSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    InetAddress address = InetAddress.getByName("228.5.6.7");

                    // Create a buffer of bytes, which will be used to store
                    // the incoming bytes containing the information from the server.
                    // Since the message is small here, 256 bytes should be enough.
                    byte[] buf = new byte[256];

                    // Create a new Multicast socket (that will allow other sockets/programs
                    // to join it as well.
                    MulticastSocket clientSocket = new MulticastSocket(Common.UDP_IP_SCANNER_PORT);

                    //Joint the Multicast group.
                    clientSocket.joinGroup(address);

                    while (true) {
                        // Receive the information and print it.
                        DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                        clientSocket.receive(msgPacket);

                        String msg = new String(buf, 0, buf.length);
                        System.out.println("Socket 1 received msg: " + msg);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    /*
     * Get Ip address from response
     */
    public void udpClient(final String ip, final String reqString) {
        HandlerUtil.postBackground(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] buf = reqString.getBytes();


                    DatagramPacket packet = new DatagramPacket(buf, buf.length,
                            InetAddress.getByName(ip), Common.UDP_IP_SCANNER_PORT);

                    if (receiverSocket == null) {
                        receiverSocket = new DatagramSocket();
                    }

                    receiverSocket.send(packet);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
