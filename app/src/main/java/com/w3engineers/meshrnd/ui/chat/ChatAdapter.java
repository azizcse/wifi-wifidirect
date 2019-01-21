package com.w3engineers.meshrnd.ui.chat;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 11/30/2018 at 6:43 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 11/30/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ItemTextMessageInBinding;
import com.w3engineers.meshrnd.databinding.ItemTextMessageOutBinding;
import com.w3engineers.meshrnd.model.Message;
import com.w3engineers.meshrnd.ui.base.BaseAdapter;
import com.w3engineers.meshrnd.ui.base.BaseViewHolder;


public class ChatAdapter extends BaseAdapter<Message> {
    private final int TEXT_IN = 1;
    private final int TEXT_OUT = 2;

    @Override
    public int getItemViewType(int position) {
        Message item = getItem(position);
        if (item == null)
            return TEXT_OUT;

        if (item.incoming) {
            return TEXT_IN;
        } else {
            return TEXT_OUT;
        }
    }

    @Override
    public boolean isEqual(Message left, Message right) {
        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = null;
        switch (viewType) {
            case TEXT_IN:
                baseViewHolder = new TextInHolder(inflate(parent, R.layout.item_text_message_in));
                break;
            case TEXT_OUT:
                baseViewHolder = new TextOutHolder(inflate(parent, R.layout.item_text_message_out));
                break;
        }
        return baseViewHolder;
    }


    private class TextInHolder extends BaseViewHolder<Message> {

        public TextInHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(Message item, ViewDataBinding viewDataBinding) {
            ItemTextMessageInBinding binding = (ItemTextMessageInBinding) viewDataBinding;
            binding.setMessage(item);
        }

        @Override
        public void onClick(View v) {

        }
    }

    private class TextOutHolder extends BaseViewHolder<Message> {

        public TextOutHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(Message item, ViewDataBinding viewDataBinding) {
            ItemTextMessageOutBinding binding = (ItemTextMessageOutBinding) viewDataBinding;
            binding.setMessage(item);
        }

        @Override
        public void onClick(View v) {

        }
    }

}
