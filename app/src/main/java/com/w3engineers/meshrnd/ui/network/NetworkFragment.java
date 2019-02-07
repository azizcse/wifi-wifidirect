package com.w3engineers.meshrnd.ui.network;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.common.GroupServiceDevice;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.FragmentNetworkBinding;
import com.w3engineers.meshrnd.ui.base.BaseFragment;
import com.w3engineers.meshrnd.ui.base.ItemClickListener;
import com.w3engineers.meshrnd.ui.group.BottomNavActivity;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.HandlerUtil;
import com.w3.meshlib.data.SharedPref;

import java.util.List;

public class NetworkFragment extends BaseFragment implements ItemClickListener<GroupServiceDevice> {
    private FragmentNetworkBinding binding;
    private NetworkAdapter networkAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_network, container, false);
        networkAdapter = new NetworkAdapter();
        networkAdapter.setItemClickListener(this);
        binding.recyclerView.setAdapter(networkAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.progressBar.setVisibility(View.GONE);
        binding.textTitle.setText(SharedPref.read(Constants.NAME) + "'s address");
        binding.userAddress.setText(SharedPref.read(Constants.USER_ID));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDeviceInfo();
    }


    public void updateDeviceInfo() {
        String name = SharedPref.read(Constants.DEVICE_NAME);
        boolean isMaster = SharedPref.readBoolean(Constants.KEY_STATUS);
        String group = "Client";
        if (isMaster) {
            group = "Master";
        }
        binding.textName.setText(name + " =:" + group);

        ((BottomNavActivity)getActivity()).getCashData();
    }


    public void showGroupList(final List<GroupServiceDevice> devices) {
        HandlerUtil.postForeground(new Runnable() {
            @Override
            public void run() {
                for (GroupServiceDevice item : devices)
                networkAdapter.addItem(item);
            }
        });
    }

    @Override
    public void onItemClick(View view, GroupServiceDevice item) {
        ((BottomNavActivity)getActivity()).connectToTheGroup(item);
    }
}
