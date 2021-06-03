package com.w3engineers.meshrnd.ui.group;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.w3.meshlib.common.WiFiDirectBroadcastReceiver;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.controller.WiFiDirectController;
import com.w3.meshlib.util.GoFoundListener;
import com.w3.meshlib.util.P2pDevice;
import com.w3.meshlib.util.WifiConnector;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityCreateGroupBinding;
import com.w3engineers.meshrnd.model.Message;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.ui.base.ItemClickListener;
import com.w3engineers.meshrnd.util.JsonParser;
import com.w3engineers.meshrnd.util.PermissionUtil;
import com.w3engineers.meshrnd.wifi.udp.UdpScanner;
import com.w3engineers.meshrnd.wifidirect.WifiDirectManager;

import java.util.UUID;

public class GroupActivity extends AppCompatActivity implements GroupCreationDialog.GroupCreationAcceptButtonListener,
        GoFoundListener, ItemClickListener<P2pDevice> {

    private static final String TAG = GroupActivity.class.getSimpleName();

    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WiFiDirectController controller;
    private ActivityCreateGroupBinding createGroupBinding;
    private GoAdapter goAdapter;

    private WifiDirectManager wifiDirectManager;

    private UdpScanner udpScanner;


    private final int GO_SEARCH_REQUEST = 2;
    private final int GO_CREATE_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        createGroupBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);
        goAdapter = new GoAdapter();
        goAdapter.setItemClickListener(this);
        createGroupBinding.rv.setAdapter(goAdapter);
        createGroupBinding.rv.setLayoutManager(new LinearLayoutManager(this));


        controller = WiFiDirectController.on(getApplicationContext());
        wiFiDirectBroadcastReceiver = WiFiP2PInstance.getInstance(getApplicationContext()).getBroadcastReceiver();
        controller.setListener(this);

        wifiDirectManager = WifiDirectManager.on(getApplicationContext());

        udpScanner = new UdpScanner(this, null);
    }

    private void checkPermissionAndSearchGo() {
        if (PermissionUtil.init(this).request(GO_SEARCH_REQUEST, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            controller.searchGo();
        }
    }

    private void checkPermission() {
        if (PermissionUtil.init(this).request(GO_CREATE_REQUEST, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // registerBroadcastReceiver();

            controller.createGo();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == GO_SEARCH_REQUEST) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        //  Toast.makeText(this,"Permission denaid", Toast.LENGTH_LONG).show();
                        Toast.makeText(GroupActivity.this, "Please allow permission from setting", Toast.LENGTH_LONG).show();
                    } else {
                        checkPermissionAndSearchGo();
                    }
                } else {
                    checkPermissionAndSearchGo();
                }
            }
        } else if (requestCode == GO_CREATE_REQUEST) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        //  Toast.makeText(this,"Permission denied", Toast.LENGTH_LONG).show();
                        Toast.makeText(GroupActivity.this, "Please allow permission from setting", Toast.LENGTH_LONG).show();
                    } else {
                        checkPermission();
                    }
                } else {
                    checkPermission();
                }
            }
        }
    }

    /* private void registerBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtil.init(this).isAllowed(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // registerBroadcastReceiver();
        }

        Log.i("WiFiDirectService", "btoadcast receiver registeded");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(wiFiDirectBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onAcceptButtonListener(final String groupName) {

    }

    @Override
    public void onItemClick(View view, P2pDevice item) {
        int id = view.getId();
        if (id == R.id.button_join_go) {
            joinAGroup(item);
            item.isP2p = true;
        } else if (id == R.id.button_connect_connect_go) {
            item.isP2p = false;
            connectWithGoAsLc(item);

        } else if (id == R.id.user_card) {
            Message message = new Message();
            message.friendsId = "asjk";
            message.message = "hello";
            message.messageId = UUID.randomUUID().toString();


            //sendUdpMessage("192.168.49.255", message);
            //sendUdpMessage("192.168.49.1", message);

            if (item.isP2p) {
                wifiDirectManager.sendTextMessage("192.168.49.1", message, item.isP2p);
            } else {
                sendUdpMessage("192.168.49.255", message);
                //sendUdpMessage("192.168.49.1", message);
            }

           /* if (item.isP2p) {
                wifiDirectManager.sendTextMessage("192.168.49.1", message, item.isP2p);

            } else {
                sendUdpMessage("192.168.49.255", message);
            }*/

            //sendUdpMessage("192.168.49.1", message);
            //wifiDirectManager.sendTextMessage("192.168.49.1", message, item.isP2p);
        }
    }

    private void joinAGroup(P2pDevice device) {
        UserModel userModel = new UserModel();
        userModel.setIp(device.getMac());
        wifiDirectManager.createConnection(userModel);
    }

    private void connectWithGoAsLc(P2pDevice device) {
        WifiConnector connector = new WifiConnector(this);
        boolean isConnected = connector.connect(device.getSsid(), device.getPassword(), device.getMac());

        Log.d(TAG, "Wifi connection as LC: " + isConnected);
    }


    private void startGroupChatActivity(String groupName, boolean isGroupOwner) {
        Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP_NAME, groupName);
        intent.putExtra(GroupChatActivity.EXTRA_IS_GROUP_OWNER, isGroupOwner);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_item, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.broadcast_user:
                //startActivity(new Intent(this, GroupActivity.class));
            case R.id.create_group:
                checkPermission();
                break;
            case R.id.search_group:
                checkPermissionAndSearchGo();
                break;

            case R.id.init_multi_socket:
                initMultiSocket();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onGoFound(P2pDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (device != null) {
                  /*  if (device.getSsid().contains("10")) {
                        device.isP2p = false;
                    } else {
                        device.isP2p = true;
                    }*/
                    goAdapter.addItem(device);
                }
            }
        });

    }

    private void sendUdpMessage(String ip, Message message) {
        Log.d(TAG, "Sending message to UDP");
        String sendValue = JsonParser.buildMessage(message);
        udpScanner.udpClient(ip, sendValue);
    }

    private void initMultiSocket() {
        udpScanner.startSocket();
        udpScanner.udpServer("");

        //udpScanner.UDPMultiCastSocket();
    }

}
