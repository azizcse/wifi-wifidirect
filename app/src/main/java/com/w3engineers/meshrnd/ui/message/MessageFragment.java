package com.w3engineers.meshrnd.ui.message;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.meshrnd.R;
import com.w3engineers.meshrnd.databinding.FragmentMessageBinding;
import com.w3engineers.meshrnd.ui.base.BaseFragment;

public class MessageFragment extends BaseFragment {
    private FragmentMessageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false);
        return binding.getRoot();

    }
}
