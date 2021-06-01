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
import com.w3.meshlib.util.GoFoundListener;
import com.w3.meshlib.util.P2pDevice;
import com.w3.meshlib.util.WifiConnector;

import java.util.Map;


public class WiFiDirectClient implements ConnectionInfoListener, ServiceDisconnectedListener {

    private static final String TAG = "WificonnectionTest";
    private DnsSdTxtRecordListener dnsSdTxtRecordListener;
    private DnsSdServiceResponseListener dnsSdServiceResponseListener;

    private WiFiP2PInstance wiFiP2PInstance;
    private WifiConnector wifiConnector;
    private GoFoundListener goFoundListener;

    public WiFiDirectClient(Context context, WifiConnector wifiConnector) {
        wiFiP2PInstance = WiFiP2PInstance.getInstance(context);
        wiFiP2PInstance.setServerDisconnectedListener(this);
        this.wifiConnector = wifiConnector;
    }

    public void initializeServicesDiscovery() {
        // We need to start discovering peers to activate the service search
        wiFiP2PInstance.startPeerDiscovering();
        setupDnsListeners();
        triggeredGoDiscovery();
    }

    public void stopServiceDiscovery() {
        dnsSdTxtRecordListener = null;
        dnsSdServiceResponseListener = null;
        wiFiP2PInstance.getWifiP2pManager().setDnsSdResponseListeners(wiFiP2PInstance.getChannel(),
                null, null);

        wiFiP2PInstance.getWifiP2pManager().clearServiceRequests(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.e("P2p_seach", "Stop p2p search success");
            }

            public void onFailure(int reason) {
                Log.e("P2p_seach", "Stop p2p search success");
            }
        });
    }

    public void searchGO() {
        wiFiP2PInstance.startPeerDiscovering();
        setupDnsListeners();
        triggeredGoDiscovery();
    }


    private void triggeredGoDiscovery() {
        isFound = false;
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

    boolean isFound = false;

    private DnsSdTxtRecordListener getTxtRecordListener() {
        return new DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, final WifiP2pDevice device) {

                if (txtRecordMap.containsKey("pa_ss") ) {
                    Log.e(TAG, "Discovered ssid : " + txtRecordMap.toString());
                    final String ssid = txtRecordMap.get("ss_id");
                    final String pass = txtRecordMap.get("pa_ss");
                    P2pDevice p2pDevice = new P2pDevice(ssid, pass, device.deviceAddress);
                    goFoundListener.onGoFound(p2pDevice);
                }
            }
        };
    }

    private DnsSdServiceResponseListener getServiceResponseListener() {
        return new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.e(TAG, "Discovered wifi service...... success name: " + instanceName + " Type: " + registrationType);
            }
        };
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

    }

    @Override
    public void onServerDisconnectedListener() {

    }


    public void setListener(GoFoundListener listener) {
        this.goFoundListener = listener;
    }
}
