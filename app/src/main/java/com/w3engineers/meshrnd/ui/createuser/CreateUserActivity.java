package com.w3engineers.meshrnd.ui.createuser;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityCreateUserBinding;
import com.w3engineers.meshrnd.ui.group.BottomNavActivity;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.PermissionUtil;
import com.w3.meshlib.data.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.UUID;

public class CreateUserActivity extends AppCompatActivity implements View.OnClickListener {


    private ActivityCreateUserBinding mBInding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBInding = DataBindingUtil.setContentView(this, R.layout.activity_create_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBInding.createUser.setOnClickListener(this);


        if (PermissionUtil.init(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE) && PermissionUtil.init(this).request(Manifest.permission.READ_EXTERNAL_STORAGE)) {

        }

        if (!TextUtils.isEmpty(SharedPref.read(Constants.NAME)) && !TextUtils.isEmpty(SharedPref.read(Constants.USER_ID))) {
            Intent intent = new Intent(this, BottomNavActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.create_user) {
            if (!mBInding.editTextName.getText().toString().equals("")) {

                //  Long tsLong = System.currentTimeMillis() / 1000;
                //   String ts = tsLong.toString();
                //  String userId = UUID.randomUUID().toString() + ts;

                SharedPref.write(Constants.NAME, mBInding.editTextName.getText().toString());
                SharedPref.write(Constants.USER_ID, UUID.randomUUID().toString());
                Intent intent = new Intent(CreateUserActivity.this, BottomNavActivity.class);
                startActivity(intent);
                finish();

               /* final String pass = UUID.randomUUID().toString();
                ProgressDialog dialog = ProgressDialog.show(this, "",
                        "Creating account. Please wait...", true);
                dialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (!isFileExist()) {
                            createWallet(pass);
                            loadAddress();
                        } else {
                            loadAddress();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Intent intent = new Intent(CreateUserActivity.this, BottomNavActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }).start();*/


            } else {
                Snackbar.make(view, "Enter your name", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    public void loadAddress() {
        String js = loadJsonFile();
        String address = null;
        try {
            JSONObject jsonObject = new JSONObject(js);
            address = jsonObject.getString("address");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPref.write(Constants.USER_ID, address);

    }

    private void createWallet(String password) {

        String folder_main = "MeshId";

        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdir();
        }

        // pass = password.getText().toString();
        try {

            WalletUtils.generateLightNewWalletFile(password, f);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readWalletFile() {
        String fileName = "";
        File file = new File(Environment.getExternalStorageDirectory() + "/MeshId");
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                fileName = f.getName();
                //Toast.makeText(MainActivity.this,fileName,Toast.LENGTH_SHORT).show();
            }
        }
        return fileName;
    }

    public String loadJsonFile() {
        String js = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/MeshId/" + readWalletFile());

        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            int size = fileInputStream.available();

            byte[] read = new byte[size];

            fileInputStream.read(read);
            fileInputStream.close();
            js = new String(read, "UTF-8");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return js;
    }

    public boolean isFileExist() {
        File file = new File(Environment.getExternalStorageDirectory() + "/MeshId/");
        if (file.exists()) {
            return true;
        } else return false;
    }
}
