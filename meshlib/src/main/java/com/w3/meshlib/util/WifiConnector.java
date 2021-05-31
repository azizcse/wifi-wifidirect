package com.w3.meshlib.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azizul Islam on 1/19/21.
 */
public class WifiConnector {
    private WifiManager mWifiManager;
    private Context mContext;

    public WifiConnector(Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.mContext = context;
    }

    private ConnectionSuccessListener successListener = new ConnectionSuccessListener() {
        @Override
        public void success() {
            //Toast.makeText(MainActivity.this, "SUCCESS!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failed(@NonNull ConnectionErrorCode errorCode) {
            //Toast.makeText(MainActivity.this, "EPIC FAIL!" + errorCode.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public boolean connect(String ssid, String passPhrase, String macAddress) {


       if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {

           WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
           if (!wifiManager.isWifiEnabled()){
               wifiManager.setWifiEnabled(true);
           }

           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               WifiNetworkSuggestion networkSuggestion1 =
                       new WifiNetworkSuggestion.Builder()
                               .setSsid(ssid)
                               .setWpa2Passphrase(passPhrase)
                               .setIsAppInteractionRequired(false)
                               .build();


               List<WifiNetworkSuggestion> suggestionsList = new ArrayList<>();
               suggestionsList.add(networkSuggestion1);

               wifiManager.addNetworkSuggestions(suggestionsList);


               NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                       .setWpa2Passphrase(passPhrase)
                       .setSsidPattern(new PatternMatcher("DIRECT-", PatternMatcher.PATTERN_PREFIX))
                       .setBssidPattern(MacAddress.fromString(macAddress), MacAddress.fromString("ff:ff:ff:00:00:00"))
                       .setBssid(MacAddress.fromString(macAddress))
                       .setSsid(ssid)
                       .build();

               NetworkRequest request =
                       new NetworkRequest.Builder()
                               .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                               .setNetworkSpecifier(specifier)
                               .build();

               final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
               ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                   @Override
                   public void onAvailable(@NonNull Network network) {
                       super.onAvailable(network);
                       connectivityManager.bindProcessToNetwork(network);
                   }

               };

               connectivityManager.requestNetwork(request, networkCallback);
           }
           /*WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
           builder.setSsid(ssid);
           builder.setWpa2Passphrase(passPhrase);

           WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

           NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();
           networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
           networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);

           NetworkRequest nr = networkRequestBuilder1.build();
           System.out.println("Android sdk version is 29 above NetworkRequest");
           final ConnectivityManager cm = (ConnectivityManager)
                   mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
           System.out.println("Android sdk version is 29 above ConnectivityManager");
           ConnectivityManager.NetworkCallback networkCallback = new
                   ConnectivityManager.NetworkCallback()
                   {
                       @Override
                       public void onAvailable(Network network)
                       {
                           super.onAvailable(network);
                           System.out.println("onAvailabile" + network);
                           cm.bindProcessToNetwork(network);

                       }

                       @Override
                       public void onLosing(@NonNull Network network, int maxMsToLive)
                       {
                           super.onLosing(network, maxMsToLive);
                           System.out.println("onLosing" + network);
                       }

                       @Override
                       public void onLost(@NonNull Network network)
                       {
                           super.onLost(network);
                           System.out.println("onLost" + network);
                       }

                       @Override
                       public void onUnavailable()
                       {
                           super.onUnavailable();
                           System.out.println("onUnavaliable");
                       }
                   };
           System.out.println("Android sdk version is 29 above NetworkCallback");
           cm.requestNetwork(nr, networkCallback);
*/

            /*NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setWpa2Passphrase(passPhrase)
                    .setSsidPattern(new PatternMatcher("DIRECT-", PatternMatcher.PATTERN_PREFIX))
                    .setBssidPattern(MacAddress.fromString("10:03:23:00:00:00"), MacAddress.fromString("ff:ff:ff:00:00:00"))
                    .setBssid(MacAddress.fromString(macAddress))
                    .setIsHiddenSsid(true)
                    .setSsid(ssid)
                    .build();

            NetworkRequest request =
                    new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .setNetworkSpecifier(specifier)
                            .build();

            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                }

            };

            connectivityManager.requestNetwork(request, networkCallback);*/

            return true;
        } else {

            //forgetAllNetwork();

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", passPhrase);

            int networkId = getConfiguredWiFiNetworkId(ssid);
            Log.e("Highband-bt", "High band net id : " + networkId);
            if (networkId != -1) {
                wifiConfig.networkId = networkId;

                networkId = mWifiManager.updateNetwork(wifiConfig);

                if (networkId == -1) {
                    networkId = this.mWifiManager.addNetwork(wifiConfig);

                }
                Log.e("Highband-bt", "High band net id2 : " + networkId);
            } else {
                networkId = this.mWifiManager.addNetwork(wifiConfig);

            }


            Log.e("Highband-bt", "High band net id3 : " + networkId);
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(networkId, true);
            boolean status = mWifiManager.reconnect();

            return status;
        }

    }

    public int getConfiguredWiFiNetworkId(String SSID) {
        if (TextUtils.isEmpty(SSID)) {
            return -1;
        }
        List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();

        if (configuredNetworks != null) {

            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration != null && wifiConfiguration.networkId != -1) {
                    if (SSID.equals(wifiConfiguration.SSID)) {
                        return wifiConfiguration.networkId;
                    }
                }
            }
        }

        return -1;
    }


}
