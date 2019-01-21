package com.w3engineers.meshrnd.ui.chat;

import com.w3engineers.meshrnd.model.Message;

public interface MessageListener {
    void onMessageReceived(Message message);
}
