package com.w3.meshlib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.w3.meshlib.common.Constant;
import com.w3.meshlib.common.MeshLog;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BluetoothClient implements ConnectionListener {
    private BluetoothSocket bluetoothSocket;
    private Executor executor;
    private String userInfo;
    private BleListener bleListener;
    private UUID mUuid;
    private String myMac;

    public BluetoothClient(String userInformation, BleListener bleListener, String myAddress) {
        this.executor = Executors.newSingleThreadExecutor();
        this.userInfo = userInformation;
        this.bleListener = bleListener;
        mUuid = UUID.fromString(Constant.BLE_CHANNEL_PREFIX + myAddress.replace(":", ""));
        myMac = myAddress;
    }


    public void createConnection(final BluetoothDevice bluetoothDevice, final BluetoothAdapter bluetoothAdapter) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothAdapter.cancelDiscovery();
                    MeshLog.v("Connect to uuid =" + mUuid+" Name ="+bluetoothDevice.getName());

                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(mUuid);
                   // bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(mUuid);
                    bluetoothSocket.connect();
                    BleLink link = new BleLink(bluetoothSocket, BluetoothClient.this, myMac);
                    link.start();
                    link.write(userInfo.getBytes());
                    bleListener.onDeviceConnected(bluetoothDevice.getAddress(), link);
                } catch (IOException e) {
                    e.printStackTrace();
                    bleListener.onErrorOccurred("Bluetooth IOException occurred", bluetoothDevice.getAddress());
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void read(BleLink link, String data) {
        MeshLog.v("BLE Client msg received =" + data);
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
