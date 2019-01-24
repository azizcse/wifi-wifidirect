package com.w3engineers.meshrnd.ui.Nearby;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.common.GroupDevice;
import com.w3.meshlib.model.User;
import com.w3.meshlib.service.WiFiDirectService;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.FragmentNearbyBinding;
import com.w3engineers.meshrnd.ui.base.BaseFragment;
import com.w3engineers.meshrnd.ui.base.ItemClickListener;
import com.w3engineers.meshrnd.util.Common;

import java.util.List;

public class NearbyFragment extends BaseFragment implements ItemClickListener<GroupDevice> {
    private FragmentNearbyBinding binding;
    private NearbyAdapter nearbyAdapter;
    private WiFiDirectService wiFiDirectService;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_nearby, container, false);
        nearbyAdapter = new NearbyAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(nearbyAdapter);
        wiFiDirectService = WiFiDirectService.getInstance(getActivity(), Common.getMyInfo());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<User> userList = wiFiDirectService.getConnectedUsers();
        for (User item : userList) {
            nearbyAdapter.addItem(item);
        }
    }

    @Override
    public void onItemClick(View view, GroupDevice item) {

    }
}
