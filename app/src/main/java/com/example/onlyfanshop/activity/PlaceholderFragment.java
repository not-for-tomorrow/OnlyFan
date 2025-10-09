package com.example.onlyfanshop.ui;

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

    private static final String ARG_FEATURE_NAME = "feature_name";
    private static final String ARG_ICON = "icon";

    public static PlaceholderFragment newInstance(String featureName, String icon) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FEATURE_NAME, featureName);
        args.putString(ARG_ICON, icon);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.placeholder_fragment, container, false);

        TextView tvIcon = view.findViewById(R.id.tv_icon);
        TextView tvMessage = view.findViewById(R.id.tv_message);

        if (getArguments() != null) {
            String icon = getArguments().getString(ARG_ICON, "🚧");
            String featureName = getArguments().getString(ARG_FEATURE_NAME, "Tính năng");

            tvIcon.setText(icon);
            tvMessage.setText(featureName + " đang được phát triển");
        }

        return view;
    }
}