package com.w3engineers.meshrnd.ui.chat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ActivityChatBinding;
import com.w3engineers.meshrnd.model.Message;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.wifidirect.WifiDirectManager;

import java.util.UUID;


/**
 * * ============================================================================
 * * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * * Unauthorized copying of this file, via any medium is strictly prohibited
 * * Proprietary and confidential
 * * ----------------------------------------------------------------------------
 * * Created by: Sikder Faysal Ahmed on [15-Jan-2019 at 1:01 PM].
 * * Email: sikderfaysal@w3engineers.com
 * * ----------------------------------------------------------------------------
 * * Project: meshrnd.
 * * Code Responsibility: <Purpose of code>
 * * ----------------------------------------------------------------------------
 * * Edited by :
 * * --> <First Editor> on [15-Jan-2019 at 1:01 PM].
 * * --> <Second Editor> on [15-Jan-2019 at 1:01 PM].
 * * ----------------------------------------------------------------------------
 * * Reviewed by :
 * * --> <First Reviewer> on [15-Jan-2019 at 1:01 PM].
 * * --> <Second Reviewer> on [15-Jan-2019 at 1:01 PM].
 * * ============================================================================
 **/
public class ChatActivity extends AppCompatActivity implements View.OnClickListener, MessageListener {

    private ActivityChatBinding mBinding;
    private UserModel userModel;

    private ChatAdapter chatAdapter;
    private WifiDirectManager wifiDirectManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        userModel = (UserModel) getIntent().getSerializableExtra(UserModel.class.getName());
        getSupportActionBar().setTitle(userModel.getUserName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.imageButtonSend.setOnClickListener(this);
        chatAdapter = new ChatAdapter();

        wifiDirectManager = WifiDirectManager.on(getApplicationContext());
        wifiDirectManager.initMessageListener(this);

        mBinding.recyclerViewMessage.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerViewMessage.setAdapter(chatAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiDirectManager.initMessageListener(null);
    }

    @Override
    public void onClick(View v) {
        String inputValue = mBinding.edittextMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(inputValue)) return;
        Message message = buildMessage(inputValue);
        wifiDirectManager.sendTextMessage(userModel.getIp(), message);
        mBinding.edittextMessageInput.setText("");
        chatAdapter.addItem(message);
        scrollSmoothly();
    }

    private Message buildMessage(String msg) {
        Message message = new Message();
        message.message = msg;
        message.messageId = UUID.randomUUID().toString();
        message.incoming = false;
        return message;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageReceived(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.addItem(message);
                scrollSmoothly();

            }
        });
    }

    private void scrollSmoothly() {
        mBinding.recyclerViewMessage.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
    }
}
