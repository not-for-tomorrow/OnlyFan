package com.example.onlyfanshop.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlyfanshop.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";

    public static PlaceholderFragment newInstance(String title) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.placeholder_fragment, container, false);
        TextView tv = v.findViewById(R.id.placeholderText);
        if (getArguments() != null) {
            tv.setText(getArguments().getString(ARG_TITLE, "Tab"));
        }
        return v;
    }
}