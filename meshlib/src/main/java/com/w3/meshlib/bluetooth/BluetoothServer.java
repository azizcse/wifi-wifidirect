package com.w3.meshlib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.w3.meshlib.common.Constant;
import com.w3.meshlib.common.MeshLog;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BluetoothServer implements ConnectionListener {

    private BluetoothAdapter bluetoothAdapter;
    private String userInformation;
    private ConnectionListenThread connectionListenThread;
    private BleListener bleListener;

    private List<ConnectionListenThread> bluetoothServerList;

    public BluetoothServer(String userInfo, BleListener bleListener) {

        this.userInformation = userInfo;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bleListener = bleListener;
    }




    public void createChannelForSpecificClient(String address) {
        /*if(bluetoothServerList.size()>= 7) {
            MeshLog.v("Max server is running");
            return;
        }*/
        MeshLog.v("Create server for address ="+address);
        connectionListenThread = new ConnectionListenThread(address);
        connectionListenThread.start();
        //bluetoothServerList.add(connectionListenThread);
    }

    public void startListenThread(){
       /*
        if (connectionListenThread == null || !connectionListenThread.isRunning()) {
            connectionListenThread = new ConnectionListenThread();
            connectionListenThread.start();
        }*/
    }

    public void stopListenThread() {
        if (connectionListenThread != null) {
            MeshLog.v("BLE Server stopped");
            connectionListenThread.stop();
            connectionListenThread = null;
        }
    }


    private class ConnectionListenThread implements Runnable {

        private Thread thread;
        private boolean isRunning;
        private BluetoothServerSocket bluetoothServerSocket;
        private UUID mUUID;
        private String clientMacAddress;
        public ConnectionListenThread(String clientAddress) {
            thread = new Thread(this);
            thread.setDaemon(true);
            mUUID = UUID.fromString(Constant.BLE_CHANNEL_PREFIX + clientAddress.replace(":", ""));
            this.clientMacAddress = clientAddress;
        }

        @Override
        public void run() {
            //while (isRunning) {
                //MeshLog.v("BLE Server running");
                try {
                    MeshLog.v("Create server uuid ="+mUUID);

                    bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constant.NAME_INSECURE, mUUID);
                    //bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constant.NAME_SECURE, mUUID);
                    BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();
                    BleLink link = new BleLink(bluetoothSocket, BluetoothServer.this, clientMacAddress);
                    link.start();
                    link.write(userInformation.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    isRunning = false;
                }
            //}
        }

        synchronized void start() {
            if (isRunning) {
                return;
            }
            isRunning = true;
            thread.start();
        }

        synchronized void stop() {
            if (!isRunning) {
                return;
            }
            isRunning = false;
            thread.interrupt();
        }

        boolean isRunning() {
            return isRunning;
        }
    }


    @Override
    public void read(BleLink link, String data) {
        MeshLog.v("BLE server msg received ="+data);
        bleListener.onReceivedMessage(link, data);
    }

    @Override
    public void close(BleLink link) {
        link.close();
    }

    @Override
    public void onConnectionClosed(String mac) {
        bleListener.onConnectionLost(mac);
    }
}
