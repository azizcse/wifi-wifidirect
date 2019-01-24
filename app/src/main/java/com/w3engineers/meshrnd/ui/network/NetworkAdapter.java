package com.w3engineers.meshrnd.ui.network;

import android.content.ClipData;
import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.common.GroupServiceDevice;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ItemGroupLayoutBinding;
import com.w3engineers.meshrnd.ui.base.BaseAdapter;
import com.w3engineers.meshrnd.ui.base.BaseViewHolder;

public class NetworkAdapter extends BaseAdapter<GroupDevice> {
    private final int WIFI_P2P_GROUP = 0;

    @Override
    public int getItemViewType(int position) {
        GroupDevice groupDevice = getItem(position);
        if(groupDevice instanceof GroupServiceDevice){
            return WIFI_P2P_GROUP;
        }

        return -1;
    }

    @Override
    public boolean isEqual(GroupDevice left, GroupDevice right) {
        return left.getDeviceMac().equals(right.getDeviceMac());
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(inflate(parent, R.layout.item_group_layout));
    }

    private class GroupViewHolder extends BaseViewHolder<GroupServiceDevice>{

        public GroupViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(GroupServiceDevice item, ViewDataBinding viewDataBinding) {
            ItemGroupLayoutBinding binding = (ItemGroupLayoutBinding)viewDataBinding;
            binding.textViewTime.setText(item.getDeviceMac());
            binding.userName.setText(item.getGroupName());
        }

        @Override
        public void onClick(View v) {

        }
    }
}
