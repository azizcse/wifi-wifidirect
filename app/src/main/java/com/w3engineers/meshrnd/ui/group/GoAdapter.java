package com.w3engineers.meshrnd.ui.group;

import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.util.P2pDevice;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ItemDiscoveredUserBinding;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.ui.base.BaseAdapter;
import com.w3engineers.meshrnd.ui.base.BaseViewHolder;
import com.w3engineers.meshrnd.ui.main.UserListAdapter;

/**
 * Created by Azizul Islam on 6/1/21.
 */
public class GoAdapter extends BaseAdapter<P2pDevice> {
    @Override
    public boolean isEqual(P2pDevice left, P2pDevice right) {
        return left.getMac().equals(right.getMac());
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(inflate(parent, R.layout.item_discovered_user));
    }

    public class UserViewHolder extends BaseViewHolder<P2pDevice> {

        public UserViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(P2pDevice item, ViewDataBinding viewDataBinding) {
            ItemDiscoveredUserBinding binding = (ItemDiscoveredUserBinding) viewDataBinding;

            setClickListener(binding.userCard, binding.buttonConnectConnectGo, binding.buttonJoinGo);
            //binding.userCard.setOnClickListener(this);

            binding.userName.setText(item.getSsid());
            binding.textViewTime.setText(item.getMac());
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getItem(getAdapterPosition()));
        }
    }
}
