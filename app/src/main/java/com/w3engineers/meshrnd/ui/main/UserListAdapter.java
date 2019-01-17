package com.w3engineers.meshrnd.ui.main;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.ItemDiscoveredUserBinding;
import com.w3engineers.meshrnd.model.UserModel;
import com.w3engineers.meshrnd.ui.base.BaseAdapter;
import com.w3engineers.meshrnd.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * * ============================================================================
 * * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * * Unauthorized copying of this file, via any medium is strictly prohibited
 * * Proprietary and confidential
 * * ----------------------------------------------------------------------------
 * * Created by: Sikder Faysal Ahmed on [15-Jan-2019 at 5:14 PM].
 * * Email: sikderfaysal@w3engineers.com
 * * ----------------------------------------------------------------------------
 * * Project: meshrnd.
 * * Code Responsibility: <Purpose of code>
 * * ----------------------------------------------------------------------------
 * * Edited by :
 * * --> <First Editor> on [15-Jan-2019 at 5:14 PM].
 * * --> <Second Editor> on [15-Jan-2019 at 5:14 PM].
 * * ----------------------------------------------------------------------------
 * * Reviewed by :
 * * --> <First Reviewer> on [15-Jan-2019 at 5:14 PM].
 * * --> <Second Reviewer> on [15-Jan-2019 at 5:14 PM].
 * * ============================================================================
 **/
public class UserListAdapter extends BaseAdapter<UserModel> {


    private Context mContext;


    public UserListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean isEqual(UserModel left, UserModel right) {
        if(left.getIp() == null || right.getIp() == null){
            return false;
        }
        if(left.getIp().equals(right.getIp())){
            return true;
        }

        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(inflate(parent, R.layout.item_discovered_user));
    }

    public class UserViewHolder extends BaseViewHolder<UserModel>{

        public UserViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(UserModel item, ViewDataBinding viewDataBinding) {
            ItemDiscoveredUserBinding binding = (ItemDiscoveredUserBinding) viewDataBinding;
            binding.userCard.setOnClickListener(this);
            binding.userName.setText(item.getUserName());
            binding.textViewTime.setText(item.getIp());
        }

        @Override
        public void onClick(View v) {
           mItemClickListener.onItemClick(v, getItem(getAdapterPosition()));
        }
    }
}
