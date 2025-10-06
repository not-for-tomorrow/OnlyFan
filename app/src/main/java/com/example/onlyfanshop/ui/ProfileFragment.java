package com.example.onlyfanshop.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.onlyfanshop.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {

    private CardView btnEditProfile;
    private View btnMyStores, btnSupport, btnPinCode, btnLogout;
    private SwitchCompat switchPushNotif, switchFaceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnMyStores = view.findViewById(R.id.btnMyStores);
        btnSupport = view.findViewById(R.id.btnSupport);
        btnPinCode = view.findViewById(R.id.btnPinCode);
        btnLogout = view.findViewById(R.id.btnLogout);
        switchPushNotif = view.findViewById(R.id.switchPushNotif);
        switchFaceId = view.findViewById(R.id.switchFaceId);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to Edit Profile screen
            Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        btnMyStores.setOnClickListener(v -> {
            // TODO: Navigate to My Stores screen
            Toast.makeText(requireContext(), "My Stores clicked", Toast.LENGTH_SHORT).show();
        });

        btnSupport.setOnClickListener(v -> {
            // TODO: Navigate to Support screen
            Toast.makeText(requireContext(), "Support clicked", Toast.LENGTH_SHORT).show();
        });

        btnPinCode.setOnClickListener(v -> {
            // TODO: Navigate to PIN Code settings
            Toast.makeText(requireContext(), "PIN Code clicked", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        switchPushNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Handle push notification preference
            Toast.makeText(requireContext(),
                    "Push notifications: " + (isChecked ? "ON" : "OFF"),
                    Toast.LENGTH_SHORT).show();
        });

        switchFaceId.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Handle Face ID preference
            Toast.makeText(requireContext(),
                    "Face ID: " + (isChecked ? "ON" : "OFF"),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // TODO: Implement logout logic
                    Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    // Navigate to login screen if needed
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}