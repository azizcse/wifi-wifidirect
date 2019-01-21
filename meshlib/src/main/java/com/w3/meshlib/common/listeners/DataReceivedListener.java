package com.w3.meshlib.common.listeners;


import com.w3.meshlib.common.messages.MessageWrapper;

public interface DataReceivedListener {

    void onDataReceived(MessageWrapper messageWrapper);

}
