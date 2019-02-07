package com.w3.meshlib.bluetooth;

import android.bluetooth.BluetoothSocket;

import com.w3.meshlib.common.MeshLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BleLink extends Thread {
    private BluetoothSocket bSocket;
    private InputStream in;
    private OutputStream out;
    private volatile boolean runner;
    private ConnectionListener connectionListener;
    private String clientAddress;
    public BleLink(BluetoothSocket bluetoothSocket, ConnectionListener connectionListener, String clientMacAddress) {
        try {
            this.bSocket = bluetoothSocket;
            this.in = bluetoothSocket.getInputStream();
            this.out = bluetoothSocket.getOutputStream();
            this.runner = true;
            this.connectionListener = connectionListener;
            setName(UUID.randomUUID().toString());
            this.clientAddress = clientMacAddress;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void write(byte[] data) {
        try {
            this.out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if(bSocket != null) {
                this.runner = false;
                this.in.close();
                this.out.close();
                this.bSocket.close();
                this.bSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        while (runner) {
            try {
                bytes = this.in.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                connectionListener.read(this, readMessage);

            } catch (IOException e) {
                MeshLog.v("BLE Connection IOException");
                connectionListener.onConnectionClosed(clientAddress);
                e.printStackTrace();
                close();
                runner = false;
            }
        }
    }

}
