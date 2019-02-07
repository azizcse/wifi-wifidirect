package com.w3engineers.meshrnd.ui.Nearby;

import android.databinding.ViewDataBinding;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.model.User;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ItemBleUserBinding;
import com.w3engineers.meshrnd.databinding.ItemDiscoveredUserBinding;
import com.w3engineers.meshrnd.ui.base.BaseAdapter;
import com.w3engineers.meshrnd.ui.base.BaseViewHolder;

public class NearbyAdapter extends BaseAdapter<User> {
    private final int P2P_USER = 0;
    private final int BLE_USER = 1;

    @Override
    public int getItemViewType(int position) {
        User user =getItem(position);
        if(user.isBleUser()){
            return BLE_USER;
        }else {
           return P2P_USER;
        }
    }

    @Override
    public boolean isEqual(User left, User right) {
        return left.getDeviceMac().equals(right.getDeviceMac());
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        if(viewType == P2P_USER) {
            return new P2pUserHolder(inflate(parent, R.layout.item_discovered_user));
        }else {
            return new BleUserHolder(inflate(parent, R.layout.item_ble_user));
        }
    }

    private class P2pUserHolder extends BaseViewHolder<User>{

        public P2pUserHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(User item, ViewDataBinding viewDataBinding) {
            ItemDiscoveredUserBinding binding = (ItemDiscoveredUserBinding)viewDataBinding;
            binding.userCard.setOnClickListener(this);
            binding.userName.setText(item.getUserName());
            binding.textViewTime.setText(item.getIpAddress());
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getItem(getAdapterPosition()));
        }
    }

    private class BleUserHolder extends BaseViewHolder<User>{

        public BleUserHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(User item, ViewDataBinding viewDataBinding) {
            ItemBleUserBinding binding = (ItemBleUserBinding)viewDataBinding;
            binding.userCard.setOnClickListener(this);
            binding.userName.setOnClickListener(this);
            binding.textViewTime.setOnClickListener(this);

            if(TextUtils.isEmpty(item.getUserName())){
                binding.userName.setText(item.getDeviceName());
            }else {
                binding.userName.setText(item.getUserName());
            }
            binding.textViewTime.setText(item.getDeviceMac());
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getItem(getAdapterPosition()));
        }
    }
}
