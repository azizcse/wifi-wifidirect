package com.w3engineers.meshrnd.ui.group;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.w3.meshlib.common.WiFiDirectBroadcastReceiver;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.controller.WiFiDirectController;
import com.w3.meshlib.util.GoFoundListener;
import com.w3.meshlib.util.P2pDevice;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityCreateGroupBinding;
import com.w3engineers.meshrnd.util.PermissionUtil;

public class GroupActivity extends AppCompatActivity implements GroupCreationDialog.GroupCreationAcceptButtonListener, GoFoundListener {

    private static final String TAG = GroupActivity.class.getSimpleName();

    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WiFiDirectController controller;
    private ActivityCreateGroupBinding createGroupBinding;
    private GoAdapter goAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        createGroupBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);
        goAdapter = new GoAdapter();
        createGroupBinding.rv.setAdapter(goAdapter);
        createGroupBinding.rv.setLayoutManager(new LinearLayoutManager(this));


        controller = WiFiDirectController.on(getApplicationContext());
        wiFiDirectBroadcastReceiver = WiFiP2PInstance.getInstance(getApplicationContext()).getBroadcastReceiver();
        controller.setListener(this);
    }

    private void checkPermissionAndSearchGo(){
        if(PermissionUtil.init(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)){
            controller.searchGo();
        }
    }

    private void checkPermission(){
        if(PermissionUtil.init(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)){
           // registerBroadcastReceiver();

            controller.createGo();
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
        if(PermissionUtil.init(this).isAllowed(Manifest.permission.ACCESS_FINE_LOCATION)){
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
                goAdapter.addItem(device);
            }
        });

    }
}
