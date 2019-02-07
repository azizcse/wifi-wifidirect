package com.w3engineers.meshrnd.wifidirect;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/15/2019 at 11:16 AM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/15/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */


import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MessageSender {

    /**
     * <p>Instance variable</p>
     */


    //Single thread executor
    private Executor singleThreadExecutor;

    /**
     * Public constructor
     */
    public MessageSender() {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * <p>Message sender thread</p>
     *
     * @param message   : String
     * @param ipAddress : ip address
     */
    public void sendMessage(final String message, final String ipAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket();
                    AppLog.d("Start sending message");
                    InetAddress addr = InetAddress.getByName(ipAddress);
                    SocketAddress sockaddr = new InetSocketAddress(addr, Common.MESSAGING_PORT);
                    socket.connect(sockaddr, 8000);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.write(message.getBytes());
                    dos.flush();
                    dos.close();
                    socket.close();
                    AppLog.d("Close sender socket");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    AppLog.d("sender UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    AppLog.d("sender IOException");
                } finally {
                    if (socket != null && socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}
