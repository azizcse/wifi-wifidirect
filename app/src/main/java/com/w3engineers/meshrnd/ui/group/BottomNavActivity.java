package com.w3engineers.meshrnd.ui.group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.w3.meshlib.bluetooth.BluetoothManager;
import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.GroupServiceDevice;
import com.w3.meshlib.common.WiFiDirectBroadcastReceiver;
import com.w3.meshlib.common.WiFiP2PError;
import com.w3.meshlib.common.WiFiP2PInstance;
import com.w3.meshlib.common.listeners.MeshCallback;
import com.w3.meshlib.common.listeners.ServiceConnectedListener;
import com.w3.meshlib.common.listeners.ServiceDiscoveredListener;
import com.w3.meshlib.common.listeners.ServiceRegisteredListener;
import com.w3.meshlib.data.SharedPref;
import com.w3.meshlib.model.User;
import com.w3.meshlib.service.WiFiDirectService;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityCreateGroupBinding;
import com.w3engineers.meshrnd.model.Message;
import com.w3engineers.meshrnd.ui.Nearby.NearbyFragment;
import com.w3engineers.meshrnd.ui.base.BaseFragment;
import com.w3engineers.meshrnd.ui.message.MessageFragment;
import com.w3engineers.meshrnd.ui.network.NetworkFragment;
import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.JsonParser;
import com.w3engineers.meshrnd.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class BottomNavActivity extends AppCompatActivity implements GroupCreationDialog.GroupCreationAcceptButtonListener, MeshCallback {

    private static final String TAG = BottomNavActivity.class.getSimpleName();

    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WiFiDirectService wifiDirectService;

    private GroupCreationDialog groupCreationDialog;
    private BaseFragment mCurrentFragment;
    private ActivityCreateGroupBinding mBinding;
    private List<GroupServiceDevice> foundGroupService = new ArrayList<>();

    private BluetoothManager bluetoothManager;


    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationRequest mLocationRequest;

    private final int REQUEST_CHECK_SETTINGS = 0x1;


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
        initBleModule();
        getSupportActionBar().setTitle(SharedPref.read(Constants.NAME));
      //  checkLocationStatus();
        mSettingsClient = LocationServices.getSettingsClient(this);

        initLocationRequest();
        buildLocationSettingsRequest();
        buildLocationSettingClient();

    }

    void checkLocationStatus() {

        if(PermissionUtil.on().requestPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION )){
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsProviderEnabled, isNetworkProviderEnabled;
            isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Location Permission");
                builder.setMessage("The app needs location permissions. Please grant this permission to continue using the features of the app.");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.no, null);
                builder.show();
            }
        }

    }

    private void initBleModule() {
        if (PermissionUtil.init(this).request(Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            bluetoothManager = BluetoothManager.on(getApplicationContext(), Common.getMyInfo());
            bluetoothManager.initMeshCallback(this);
        }
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
                // Open settings page
                wifiDirectService.openSettings();
                //  startActivity(new Intent(this, BluetoothDemo.class));
                break;

            case R.id.menu_bluetooth_disco:
                bluetoothManager.makeDiscoverable(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void createGroup() {
        /*groupCreationDialog = new GroupCreationDialog();
        groupCreationDialog.addGroupCreationAcceptListener(this);
        groupCreationDialog.show(getSupportFragmentManager(), GroupCreationDialog.class.getSimpleName());*/

        wifiDirectService.createGroupInBackground();
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

        if (mCurrentFragment instanceof NetworkFragment) {
            ((NetworkFragment) mCurrentFragment).updateDeviceInfo();
        }
    }

    @Override
    public void onUserDiscovered(User user) {
        if (mCurrentFragment instanceof NearbyFragment) {
            ((NearbyFragment) mCurrentFragment).onUserFound(user);
        }
    }


    @Override
    public void onMessageReceived(final String msg, String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message message = JsonParser.parseMessage(msg);
                Toast.makeText(BottomNavActivity.this, message.message, Toast.LENGTH_LONG).show();
            }
        });
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
        foundGroupService.clear();
        foundGroupService.addAll(devices);
        for (GroupServiceDevice device : devices) {
            device.setGroupName(device.getTxtRecordMap().get(WiFiDirectService.SERVICE_GROUP_NAME));
        }
        if (mCurrentFragment instanceof NetworkFragment) {

            ((NetworkFragment) mCurrentFragment).showGroupList(devices);
        }
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


    private void registerBroadcastReceiver() {
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
        bluetoothManager.stopBle();
    }

    public void getCashData() {
        if (mCurrentFragment instanceof NetworkFragment) {
            ((NetworkFragment) mCurrentFragment).showGroupList(foundGroupService);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == PermissionUtil.REQUEST_CODE_PERMISSION_DEFAULT) {
//            initBleModule();
//        }
//    }

    public void connectToTheGroup(GroupServiceDevice item) {

        final ProgressDialog progressDialog = new ProgressDialog(BottomNavActivity.this);
        progressDialog.setMessage(getString(R.string.prgrss_connecting_to_group));
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        wifiDirectService.connectToService(item, new ServiceConnectedListener() {
            @Override
            public void onServiceConnected(GroupDevice serviceDevice) {
                progressDialog.dismiss();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the Location object.
     */
    private void initLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    /**
     * Build Location Settings Request
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Build Location Settings client
     */
    public void buildLocationSettingClient() {
        if (PermissionUtil.init(this).request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(this, onSuccessListener)
                    .addOnFailureListener(this, onFailureListener);
        }
    }

    /**
     * Callback for get and take success action for Location Settings client.
     */
    @SuppressLint("MissingPermission")
    private OnSuccessListener onSuccessListener = new OnSuccessListener<LocationSettingsResponse>() {
        @Override
        public void onSuccess(LocationSettingsResponse response) {
            initBleModule();
          //  mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
        }
    };

    /**
     * Callback for get and take failed action for Location Settings client.
     */
    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(BottomNavActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                    }

                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;

            }
        }
    };


    /**
     * Set location mode
     *
     * @return True or False
     */
    private boolean getLocationMode() {
        boolean value = false;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) return false;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            value = true;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            value = true;
        return value;
    }

    /**
     * Show alert for various action or error.
     */
    private void locationAlert() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Location Permission");
        builder.setMessage("The app needs location permissions. Please grant this permission to continue using the features of the app.");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, REQUEST_CHECK_SETTINGS);
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                boolean mIsLocationOn = getLocationMode();
                if (!mIsLocationOn) {
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            buildLocationSettingClient();
                            break;
                        case Activity.RESULT_CANCELED:
                            locationAlert();
                            break;
                    }
                }else{
                    initBleModule();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (String permission : permissions) {

            if (requestCode == PermissionUtil.REQUEST_CODE_PERMISSION_DEFAULT) {
                initBleModule();
            }

            else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                //denied
                buildLocationSettingClient();
            } else {
                if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    //allowed
                    buildLocationSettingClient();
                } else {
                    //set to never ask again
                }
            }
        }
    }


}
