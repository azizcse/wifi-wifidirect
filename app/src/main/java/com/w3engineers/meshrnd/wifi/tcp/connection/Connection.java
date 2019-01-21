package com.w3engineers.meshrnd.wifi.tcp.connection;

import android.util.Log;

import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.wifi.tcp.listener.ConnectionListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public class Connection extends Thread {

    /**
     * Instance variable
     */
    private ConnectionListener connectionListener;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Constructor to init socket and strum builder
     *
     * @param connectionListener : listener
     * @param socket             : socket
     * @throws IOException : io exception
     */
    public Connection(ConnectionListener connectionListener, Socket socket) throws IOException {
        this.connectionListener = connectionListener;
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            Common.closeSocket(socket);
            Log.e("Device_a", "Exception in connection");
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            String msg = in.readUTF();
            String ip = socket.getInetAddress().toString().substring(1);
            connectionListener.read(msg, ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
        }
    }
}
