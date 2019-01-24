package com.w3engineers.meshrnd.ui.Nearby;

import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.model.User;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ItemDiscoveredUserBinding;
import com.w3engineers.meshrnd.ui.base.BaseAdapter;
import com.w3engineers.meshrnd.ui.base.BaseViewHolder;

public class NearbyAdapter extends BaseAdapter<GroupDevice> {
    private final int P2P_USER = 0;

    @Override
    public int getItemViewType(int position) {
        GroupDevice groupDevice =getItem(position);

        if(groupDevice instanceof User){
            return P2P_USER;
        }

        return -1;
    }

    @Override
    public boolean isEqual(GroupDevice left, GroupDevice right) {
        return left.getDeviceMac().equals(right.getDeviceMac());
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        return new P2pUserHolder(inflate(parent, R.layout.item_discovered_user));
    }

    private class P2pUserHolder extends BaseViewHolder<User>{

        public P2pUserHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(User item, ViewDataBinding viewDataBinding) {
            ItemDiscoveredUserBinding binding = (ItemDiscoveredUserBinding)viewDataBinding;
            binding.userName.setText(item.getUserName());
            binding.textViewTime.setText(item.getDeviceMac());
        }

        @Override
        public void onClick(View v) {

        }
    }
}
