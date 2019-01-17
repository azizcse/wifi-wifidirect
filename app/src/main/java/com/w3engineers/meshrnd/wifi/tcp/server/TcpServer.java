package com.w3engineers.meshrnd.wifi.tcp.server;

import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.wifi.ScanListener;
import com.w3engineers.meshrnd.wifi.tcp.connection.Connection;
import com.w3engineers.meshrnd.wifi.tcp.listener.ConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
public class TcpServer implements ConnectionListener {

    /**
     * Instance variable
     */
    private int tcpPort;
    private ServerSocket serverSocket;
    private boolean runner = false;
    private ScanListener scanListener;
    private String myInfo;


    /**
     * <p>Server initialization</p>
     *
     * @param scanListener : listener to return data
     */
    public TcpServer(ScanListener scanListener) {
        this.scanListener = scanListener;
        //myInfo = Util.getUserInfo(null, Constant.SCAN_RES);
        tcpPort = Common.TCP_SCAN_PORT;
    }

    public void startTcpSocket() throws IOException {
        runner = true;
        serverSocket = new ServerSocket(tcpPort);
    }

    public void stopServerSocket() throws IOException {
        runner = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public void startServer(String resString) {
        myInfo = resString;
        listenThread.start();
    }

    private Thread listenThread = new Thread() {
        @Override
        public void run() {
            while (runner) {
                try {
                    AppLog.v("TCP server running");
                    Socket socket = serverSocket.accept();
                    Connection connection = new Connection(TcpServer.this, socket);
                    connection.start();
                    connection.write(myInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public void read(String msg, String ip) {
        AppLog.v("Get server =" + msg);
        scanListener.join(msg, ip);
    }
}
