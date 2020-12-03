package com.w3.meshlib.client;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.WiFiP2PError;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.common.listeners.ConnectionInfoListener;
import com.w3.meshlib.common.listeners.ServiceDisconnectedListener;
import java.util.Map;


public class WiFiDirectClient implements ConnectionInfoListener, ServiceDisconnectedListener {

    private static final String TAG = WiFiDirectClient.class.getSimpleName();
    private DnsSdTxtRecordListener dnsSdTxtRecordListener;
    private DnsSdServiceResponseListener dnsSdServiceResponseListener;

    private WiFiP2PInstance wiFiP2PInstance;
    private Boolean isRegistered = false;

    public WiFiDirectClient(Context context) {
        wiFiP2PInstance = WiFiP2PInstance.getInstance(context);
        wiFiP2PInstance.setServerDisconnectedListener(this);
    }



    public void initializeServicesDiscovery() {
        // We need to start discovering peers to activate the service search
        wiFiP2PInstance.startPeerDiscovering();
        setupDnsListeners();
        triggeredGoDiscovery();
    }


    private void triggeredGoDiscovery(){
        wiFiP2PInstance.getWifiP2pManager().clearServiceRequests(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "clearServiceRequests success");
                WifiP2pServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                wiFiP2PInstance.getWifiP2pManager().addServiceRequest(wiFiP2PInstance.getChannel(), serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "addServiceRequest success");
                        wiFiP2PInstance.getWifiP2pManager().discoverServices(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.e(TAG, "discoverServices success");
                            }
                            @Override
                            public void onFailure(int reason) {
                                WiFiP2PError wiFiP2PError = WiFiP2PError.fromReason(reason);
                                if (wiFiP2PError != null) {
                                    Log.e(TAG, "Error discovering services. Reason: " + wiFiP2PError.name());

                                }
                            }

                        });
                    }
                    @Override
                    public void onFailure(int reason) {
                    }
                });
            }
            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error clearing service request: " + reason);
            }
        });
    }

    private void setupDnsListeners() {
        if (dnsSdTxtRecordListener == null || dnsSdServiceResponseListener == null) {
            Log.e(TAG, "Service discovery listener initialized");
            dnsSdTxtRecordListener = getTxtRecordListener();
            dnsSdServiceResponseListener = getServiceResponseListener();

            wiFiP2PInstance.getWifiP2pManager().setDnsSdResponseListeners(wiFiP2PInstance.getChannel(),
                    dnsSdServiceResponseListener, dnsSdTxtRecordListener);
        }
    }

    private DnsSdTxtRecordListener getTxtRecordListener() {
        return new DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice device) {

                if (txtRecordMap.containsKey("pass")){
                    Log.e(TAG, "Discovered ssid : "+txtRecordMap.get("ssid"));
                    Log.e(TAG, "Discovered pass : "+txtRecordMap.get("pass"));
                } else {
                    Log.e(TAG, "Discovered wifi service...... success domen: "+fullDomainName);
                }
            }
        };
    }

    private DnsSdServiceResponseListener getServiceResponseListener() {
        return new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.e(TAG, "Discovered wifi service...... success name: "+instanceName+" Type: "+registrationType);
            }
        };
    }






    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

    }

    @Override
    public void onServerDisconnectedListener() {

    }

    public void stopServiceDiscovery(){

        wiFiP2PInstance.getWifiP2pManager().clearServiceRequests(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
            public void onSuccess() {

            }

            public void onFailure(int reason) {

            }
        });
    }

}
