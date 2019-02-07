package com.w3.meshlib.settings;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import com.w3.meshlib.R;
import com.w3.meshlib.common.Constant;
import com.w3.meshlib.common.MeshLog;
import com.w3.meshlib.data.SharedPref;

/**
 * * ============================================================================
 * * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * * Unauthorized copying of this file, via any medium is strictly prohibited
 * * Proprietary and confidential
 * * ----------------------------------------------------------------------------
 * * Created by: Sikder Faysal Ahmed on [29-Jan-2019 at 5:04 PM].
 * * Email: sikderfaysal@w3engineers.com
 * * ----------------------------------------------------------------------------
 * * Project: meshrnd.
 * * Code Responsibility: <Purpose of code>
 * * ----------------------------------------------------------------------------
 * * Edited by :
 * * --> <First Editor> on [29-Jan-2019 at 5:04 PM].
 * * --> <Second Editor> on [29-Jan-2019 at 5:04 PM].
 * * ----------------------------------------------------------------------------
 * * Reviewed by :
 * * --> <First Reviewer> on [29-Jan-2019 at 5:04 PM].
 * * --> <Second Reviewer> on [29-Jan-2019 at 5:04 PM].
 * * ============================================================================
 **/
public class SettingsActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiry_settings);

        final RadioButton masterButton = findViewById(R.id.radioMaster);
        RadioButton clientButton = findViewById(R.id.radioClient);

        masterButton.setChecked(SharedPref.readBoolean(Constant.PREFKEY_MASTER_ENABLE));
        clientButton.setChecked(!SharedPref.readBoolean(Constant.PREFKEY_MASTER_ENABLE));

        masterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPref.write(Constant.PREFKEY_MASTER_ENABLE, true);
                MeshLog.d("Master option clicked");
            }
        });

        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPref.write(Constant.PREFKEY_MASTER_ENABLE, false);
                 MeshLog.d("Client option clicked");
            }
        });

        final Switch bluetoothSwitch = findViewById(R.id.switch_bluetooth);

        bluetoothSwitch.setChecked(SharedPref.readBoolean(Constant.PREFKEY_BLUETOOTH_ENABLE));

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPref.write(Constant.PREFKEY_BLUETOOTH_ENABLE, b);
                 //onBluetooth(b);
              }
        });

    }

    private void onBluetooth(boolean value) {
        if (value && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            MeshLog.d("Bluetooth is Enabled");
        }else {
            if (!value && bluetoothAdapter.isEnabled()){
                bluetoothAdapter.disable();
            }
        }
    }
}
