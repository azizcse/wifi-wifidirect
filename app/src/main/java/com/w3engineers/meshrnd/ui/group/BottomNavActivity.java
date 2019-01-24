package com.w3engineers.meshrnd.ui.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.GroupServiceDevice;
import com.w3.meshlib.common.WiFiDirectBroadcastReceiver;
import com.w3.meshlib.common.WiFiP2PError;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.common.listeners.MeshCallback;
import com.w3.meshlib.common.listeners.ServiceConnectedListener;
import com.w3.meshlib.common.listeners.ServiceDiscoveredListener;
import com.w3.meshlib.common.listeners.ServiceRegisteredListener;
import com.w3.meshlib.model.User;
import com.w3.meshlib.service.WiFiDirectService;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityCreateGroupBinding;
import com.w3engineers.meshrnd.ui.Nearby.NearbyFragment;
import com.w3engineers.meshrnd.ui.base.BaseFragment;
import com.w3engineers.meshrnd.ui.message.MessageFragment;
import com.w3engineers.meshrnd.ui.network.NetworkFragment;
import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class BottomNavActivity extends AppCompatActivity implements GroupCreationDialog.GroupCreationAcceptButtonListener, MeshCallback {

    private static final String TAG = BottomNavActivity.class.getSimpleName();

    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WiFiDirectService wifiDirectService;

    private GroupCreationDialog groupCreationDialog;
    private BaseFragment mCurrentFragment;
    private ActivityCreateGroupBinding mBinding;
    private List<GroupServiceDevice> foundGroupService = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);
        wiFiDirectBroadcastReceiver = WiFiP2PInstance.getInstance(this).getBroadcastReceiver();
        wifiDirectService = WiFiDirectService.getInstance(getApplicationContext(), getMyInfo());
        wifiDirectService.initMeshCallBack(this);
        registerBroadcastReceiver();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        commitFragment(R.id.fragment_container, new NetworkFragment());
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            BaseFragment baseFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_network:
                    baseFragment = (NetworkFragment) getSupportFragmentManager()
                            .findFragmentByTag(NetworkFragment.class.getName());
                    if (baseFragment == null) {
                        baseFragment = new NetworkFragment();
                    }
                    break;
                case R.id.navigation_nearby:
                    baseFragment = (NearbyFragment) getSupportFragmentManager()
                            .findFragmentByTag(NearbyFragment.class.getName());
                    if (baseFragment == null) {
                        baseFragment = new NearbyFragment();
                    }
                    break;
                case R.id.navigation_message:
                    baseFragment = (MessageFragment) getSupportFragmentManager()
                            .findFragmentByTag(MessageFragment.class.getName());
                    if (baseFragment == null) {
                        baseFragment = new MessageFragment();
                    }
                    break;
            }
            commitFragment(R.id.fragment_container, baseFragment);
            return true;
        }
    };

    /**
     * Commit child fragment of BaseFragment on a frameLayout
     *
     * @param viewId       int value
     * @param baseFragment BaseFragment object
     * @return void
     */
    protected void commitFragment(int viewId, BaseFragment baseFragment) {
        if (baseFragment == null) return;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(viewId, baseFragment, baseFragment.getClass().getName())
                .addToBackStack(baseFragment.getClass().getName())
                .commit();

        mCurrentFragment = baseFragment;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bottom_nav, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_create_group:
                createGroup();
                break;
            case R.id.menu_find_group:
                searchAvailableGroups();
                break;
            case R.id.menu_bluetooth:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void createGroup(){
        groupCreationDialog = new GroupCreationDialog();
        groupCreationDialog.addGroupCreationAcceptListener(this);
        groupCreationDialog.show(getSupportFragmentManager(), GroupCreationDialog.class.getSimpleName());
    }


    @Override
    public void onAcceptButtonListener(final String groupName) {
        if (!groupName.isEmpty()) {
            wifiDirectService.registerService(groupName, new ServiceRegisteredListener() {

                @Override
                public void onSuccessServiceRegistered() {
                    Log.i(TAG, "Group created. Launching GroupChatActivity...");
                    //startGroupChatActivity(groupName, true);
                    groupCreationDialog.dismiss();
                }

                @Override
                public void onErrorServiceRegistered(WiFiP2PError wiFiP2PError) {
                    Toast.makeText(getApplicationContext(), "Error creating group", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            Toast.makeText(getApplicationContext(), "Please, insert a group name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeviceInfoUpdated(String deviceName, boolean status) {
        SharedPref.write(Constants.DEVICE_NAME, deviceName);
        SharedPref.write(Constants.KEY_STATUS, status);

        if(mCurrentFragment instanceof NetworkFragment){
            ((NetworkFragment)mCurrentFragment).updateDeviceInfo();
        }
    }

    private void searchAvailableGroups() {
        final ProgressDialog progressDialog = new ProgressDialog(BottomNavActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.prgrss_searching_groups));
        progressDialog.show();


        wifiDirectService.discoverServices(10000L, new ServiceDiscoveredListener() {

            @Override
            public void onNewServiceDeviceDiscovered(GroupServiceDevice serviceDevice) {
                Log.i(TAG, "New group found:");
                Log.i(TAG, "\tName: " + serviceDevice.getTxtRecordMap().get(WiFiDirectService.SERVICE_GROUP_NAME));
            }

            @Override
            public void onFinishServiceDeviceDiscovered(List<GroupServiceDevice> serviceDevices) {
                Log.i(TAG, "Found '" + serviceDevices.size() + "' groups");
                progressDialog.dismiss();

                if (serviceDevices.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_not_found_groups), Toast.LENGTH_LONG).show();
                } else {
                    showPickGroupDialog(serviceDevices);
                }
            }

            @Override
            public void onError(WiFiP2PError wiFiP2PError) {
                Toast.makeText(getApplicationContext(), "Error searching groups: " + wiFiP2PError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPickGroupDialog(final List<GroupServiceDevice> devices) {
        List<String> deviceNames = new ArrayList<>();
        foundGroupService.clear();
        foundGroupService.addAll(devices);
        for (GroupServiceDevice device : devices) {
            deviceNames.add(device.getTxtRecordMap().get(WiFiDirectService.SERVICE_GROUP_NAME));
            device.setGroupName(device.getTxtRecordMap().get(WiFiDirectService.SERVICE_GROUP_NAME));
            AppLog.v("Other properties =" + device.getTxtRecordMap().get(WiFiDirectService.DEVELOPER_NAME));

        }
        if(mCurrentFragment instanceof NetworkFragment){

            ((NetworkFragment)mCurrentFragment).showGroupList(devices);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a group");
        builder.setItems(deviceNames.toArray(new String[deviceNames.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final GroupServiceDevice serviceSelected = devices.get(which);
                final ProgressDialog progressDialog = new ProgressDialog(BottomNavActivity.this);
                progressDialog.setMessage(getString(R.string.prgrss_connecting_to_group));
                progressDialog.setIndeterminate(true);
                progressDialog.show();

                wifiDirectService.connectToService(serviceSelected, new ServiceConnectedListener() {
                    @Override
                    public void onServiceConnected(GroupDevice serviceDevice) {
                        progressDialog.dismiss();
                    }
                });
            }
        });

        AlertDialog pickGroupDialog = builder.create();
        pickGroupDialog.show();
    }


    public static User getMyInfo() {
        User user = new User();
        user.setUserName(SharedPref.read(Constants.NAME));
        user.setUserId(SharedPref.read(Constants.USER_ID));
        return user;
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    private void registerBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wiFiDirectBroadcastReceiver);
        if (wifiDirectService != null) {
            wifiDirectService.disconnect();
        }
    }

    public void getCashData() {
        if(mCurrentFragment instanceof NetworkFragment){
            ((NetworkFragment)mCurrentFragment).showGroupList(foundGroupService);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
