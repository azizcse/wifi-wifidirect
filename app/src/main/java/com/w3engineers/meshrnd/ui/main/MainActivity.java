package com.w3engineers.meshrnd.ui.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityMainBinding;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.ui.base.ItemClickListener;
import com.w3engineers.meshrnd.ui.chat.ChatActivity;
import com.w3engineers.meshrnd.ui.chat.ChatAdapter;
import com.w3engineers.meshrnd.ui.group.GroupActivity;
import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.SharedPref;
import com.w3engineers.meshrnd.wifi.WiFiScanCallBack;
import com.w3engineers.meshrnd.wifi.WifiScanManager;
import com.w3engineers.meshrnd.wifidirect.WifiDirectManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, WiFiScanCallBack, ItemClickListener<UserModel> {

    private ActivityMainBinding mBinding;
    private WifiScanManager mWifiScanManager;
    private UserListAdapter mAdapter;
    private WifiDirectManager wifiDirectManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mWifiScanManager = WifiScanManager.getInstance(getApplicationContext(), this);
        mBinding.progressBar.setVisibility(View.GONE);
        mBinding.scanBtn.setOnClickListener(this);
        mBinding.scanDirect.setOnClickListener(this);

        mAdapter = new UserListAdapter(this);
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.setItemClickListener(this);
        mAdapter.addItems(mWifiScanManager.getUserModelList());


        //WiFi direct manager
        wifiDirectManager = WifiDirectManager.on(getApplicationContext());
        wifiDirectManager.initListener(this);

        mBinding.textTitle.setText(SharedPref.read(Constants.NAME)+"'s address");
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
               startActivity(new Intent(this, GroupActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.scan_btn:
                // Scan Btn functionality
                mWifiScanManager.startMainScanner();
                mBinding.progressBar.setVisibility(View.VISIBLE);
                break;
            case R.id.scan_direct:
                // Wifi direct btn functionality
                wifiDirectManager.findPeers();
                mBinding.progressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onScanFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBinding.progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onUserFound(final UserModel userModel) {
        AppLog.v(userModel.getUserName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addItem(userModel);
            }
        });

    }

    @Override
    public void onUserFound(final List<UserModel> userModels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
             mAdapter.addItem(userModels);
            }
        });
    }

    @Override
    public void updateDeviceAddress() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String name = SharedPref.read(Constants.DEVICE_NAME);
                boolean isMaster = SharedPref.readBoolean(Constants.KEY_STATUS);
                String group = "Client";
                if (isMaster) {
                    group = "Master";
                }
                mBinding.textName.setText(name + " =:" + group);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiDirectManager.unRegisterReceiver();
    }


    @Override
    public void onItemClick(View view, UserModel item) {
        if (item.getIp().contains("192.")) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(UserModel.class.getName(), item);
            startActivity(intent);
        } else {
            wifiDirectManager.createConnection(item);
        }
    }
}
