package com.w3engineers.meshrnd.ui.main;

import android.databinding.DataBindingUtil;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityMainBinding;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.ui.base.ItemClickListener;
import com.w3engineers.meshrnd.util.AppLog;
import com.w3engineers.meshrnd.wifi.WiFiScanCallBack;
import com.w3engineers.meshrnd.wifi.WifiScanManager;
import com.w3engineers.meshrnd.wifidirect.WifiDirectManager;

import java.util.ArrayList;
import java.util.List;

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
        wifiDirectManager = WifiDirectManager.on(getApplicationContext(), this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
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
    protected void onDestroy() {
        super.onDestroy();
        wifiDirectManager.unRegisterReceiver();
    }



    @Override
    public void onItemClick(View view, UserModel item) {
        wifiDirectManager.createConnection(item);
    }
}
