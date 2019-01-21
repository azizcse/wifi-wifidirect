package com.w3engineers.meshrnd.ui.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.messages.MessageWrapper;
import com.w3engineers.meshrnd.R;

import java.util.List;


public class ChatAdapter extends ArrayAdapter<MessageWrapper> {

    private static class ChatAdapterHolder {
        TextView txtViewUsername;
        TextView txtViewMessage;
    }

    private static class ChatAdapterOwnerHolder {
        TextView txtViewMessage;
    }

    private GroupDevice currentDevice;

    public ChatAdapter(Context context, List<MessageWrapper> messages, GroupDevice currentDevice) {
        super(context, 0, messages);
        this.currentDevice = currentDevice;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MessageWrapper message = getItem(position);

        boolean isCurrentDeviceMessage = message.getWroupDevice().equals(currentDevice);
        if (isCurrentDeviceMessage) {
            ChatAdapterOwnerHolder chatAdapterOwnerHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.adapter_chat_owner, parent, false);

                chatAdapterOwnerHolder = new ChatAdapterOwnerHolder();
                chatAdapterOwnerHolder.txtViewMessage = (TextView) convertView.findViewById(R.id.text_view_message_owner);
                convertView.setTag(chatAdapterOwnerHolder);
                chatAdapterOwnerHolder.txtViewMessage.setText(message.getMessage());
            } else {
                //chatAdapterOwnerHolder = (ChatAdapterOwnerHolder) convertView.getTag();
            }


        } else {
            ChatAdapterHolder chatAdapterHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.adapter_chat, parent, false);

                chatAdapterHolder = new ChatAdapterHolder();
                chatAdapterHolder.txtViewUsername = (TextView) convertView.findViewById(R.id.text_view_username);
                chatAdapterHolder.txtViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);
                convertView.setTag(chatAdapterHolder);
            } else {
                chatAdapterHolder = (ChatAdapterHolder) convertView.getTag();
            }

            chatAdapterHolder.txtViewUsername.setText(message.getWroupDevice().getDeviceName());
            chatAdapterHolder.txtViewMessage.setText(message.getMessage());
        }

        return convertView;
    }
}
