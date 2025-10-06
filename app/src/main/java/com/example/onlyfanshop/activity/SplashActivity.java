package com.example.onlyfanshop.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlyfanshop.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding= ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Corrected line: Use startActivity (singular) instead of startActivities (plural)
        binding.startBtn.setOnClickListener(view -> startActivity(new Intent(SplashActivity.this, DashboardActivity.class)));
    }
}
