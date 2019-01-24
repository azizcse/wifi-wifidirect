package com.w3engineers.meshrnd.ui.createuser;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityCreateUserBinding;
import com.w3engineers.meshrnd.ui.group.BottomNavActivity;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.SharedPref;

import java.util.UUID;

public class CreateUserActivity extends AppCompatActivity implements View.OnClickListener{


    private ActivityCreateUserBinding mBInding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBInding = DataBindingUtil.setContentView(this, R.layout.activity_create_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBInding.createUser.setOnClickListener(this);

        if (!TextUtils.isEmpty(SharedPref.read(Constants.NAME))){
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
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                String userId = UUID.randomUUID().toString() + ts;

                SharedPref.write(Constants.NAME, mBInding.editTextName.getText().toString());
                SharedPref.write(Constants.USER_ID, userId);

                Intent intent = new Intent(CreateUserActivity.this, BottomNavActivity.class);
                startActivity(intent);
                finish();
            }else {
                Snackbar.make(view, "Enter your name", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
