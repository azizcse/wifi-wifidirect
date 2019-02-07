package com.w3engineers.meshrnd.ui.Nearby;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3.meshlib.MeshApp;
import com.w3.meshlib.bluetooth.BluetoothManager;
import com.w3.meshlib.model.User;
import com.w3.meshlib.service.WiFiDirectService;
import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.FragmentNearbyBinding;
import com.w3engineers.meshrnd.ui.base.BaseFragment;
import com.w3engineers.meshrnd.ui.base.ItemClickListener;
import com.w3engineers.meshrnd.ui.chat.ChatActivity;
import com.w3engineers.meshrnd.util.Common;
import com.w3engineers.meshrnd.util.Constants;
import com.w3engineers.meshrnd.util.HandlerUtil;
import com.w3.meshlib.data.SharedPref;

import java.util.List;

public class NearbyFragment extends BaseFragment implements ItemClickListener<User> {

    private FragmentNearbyBinding binding;
    private NearbyAdapter nearbyAdapter;
    private WiFiDirectService wiFiDirectService;
    private BluetoothManager bluetoothManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_nearby, container, false);
        nearbyAdapter = new NearbyAdapter();
        nearbyAdapter.setItemClickListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(nearbyAdapter);
        wiFiDirectService = WiFiDirectService.getInstance(getActivity(), Common.getMyInfo());
        bluetoothManager = BluetoothManager.on(MeshApp.getContext(), Common.getMyInfo());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<User> userList = wiFiDirectService.getConnectedUsers();
       String myId = SharedPref.read(Constants.USER_ID);
        for (User item : userList) {
           if(myId.equals(item.getUserId())) continue;
            nearbyAdapter.addItem(item);
        }

        List<User> bluetoothUsers = bluetoothManager.getBlueToothUsers();
        for (User item : bluetoothUsers){
            if(myId.equals(item.getUserId())) continue;

            nearbyAdapter.addItem(item);
        }
    }

    public void onUserFound(final User user){
        HandlerUtil.postForeground(new Runnable() {
            @Override
            public void run() {
                nearbyAdapter.addItem(user);
            }
        });
    }




    @Override
    public void onItemClick(View view, User item) {
        Common.currentChatUser = item;
        startActivity(new Intent(getActivity(), ChatActivity.class));
    }
}
