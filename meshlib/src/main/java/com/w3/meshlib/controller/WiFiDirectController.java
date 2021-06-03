package com.w3.meshlib.controller;

import android.content.Context;

import com.w3.meshlib.client.WiFiDirectClient;
import com.w3.meshlib.service.WiFiDirectService;
import com.w3.meshlib.util.GoFoundListener;
import com.w3.meshlib.util.WifiConnector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Azizul Islam on 11/30/20.
 */
public class WiFiDirectController {

    private Context mContext;
    private static WiFiDirectController wiFiDirectController;
    private WiFiDirectService wiFiDirectService;
    private WiFiDirectClient wiFiDirectClient;
    private WifiConnector wifiConnector;

    private WiFiDirectController(Context context){
        this.mContext = context;
        wifiConnector = new WifiConnector(context);
        wiFiDirectService = new WiFiDirectService(context,"");
        wiFiDirectClient = new WiFiDirectClient(context, wifiConnector);
    }

    public static WiFiDirectController on(Context context){
        if(wiFiDirectController == null){
            wiFiDirectController = new WiFiDirectController(context);
        }
        return wiFiDirectController;
    }

    public void createGo(){
        stopGoSearch();
        wiFiDirectService.registerService();
    }

    public void searchGo(){
        //stopGo();
        wiFiDirectClient.initializeServicesDiscovery();
    }

    public void stopGo(){
        wiFiDirectService.stopGoAllEvent();
    }

    public void stopGoSearch(){
        wiFiDirectClient.stopServiceDiscovery();
    }

    public void stopAll(){
        stopGo();
        stopGoSearch();
    }

    public void startAll(){
        wiFiDirectService.registerService();
        wiFiDirectClient.initializeServicesDiscovery();
    }

    public void stopGoAdvertise(){
        wiFiDirectService.clearLocalServices();
    }


    public void setListener(GoFoundListener listener) {
        wiFiDirectClient.setListener(listener);
    }
}
