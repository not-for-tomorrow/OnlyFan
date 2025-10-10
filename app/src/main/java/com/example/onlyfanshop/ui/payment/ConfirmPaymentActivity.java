package com.example.onlyfanshop.ui.payment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.databinding.ActivityCartBinding;
import com.example.onlyfanshop.databinding.ActivityConfirmPaymentBinding;

public class ConfirmPaymentActivity extends AppCompatActivity {
    private ActivityConfirmPaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityConfirmPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnCancle.setOnClickListener(v -> finish());
        //String address ="";
        binding.radioAddress.setOnCheckedChangeListener((group, checkedID)->{
            if (checkedID == binding.radioBtnDfAd.getId()) {
                // Khi chọn "Text cứng"
                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String address = sharedPreferences.getString("address", "");
                if (!address.isEmpty()) {
                    binding.textViewDfAd.setVisibility(View.VISIBLE);
                    binding.textViewDfAd.setText(address);
                } else {
                    binding.edtDefaultAdress.setVisibility(View.VISIBLE);
                    binding.edtDefaultAdress.setHint("Bạn chưa có địa chỉ, vui lòng nhập địa chỉ....");
                    binding.edtDefaultAdress.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) { // rời khỏi ô nhập
                            String adress = binding.edtDefaultAdress.getText().toString();
                            Log.d("TAG", "Giá trị khi rời khỏi ô: " + adress);
                        }
                    });
                }

            } else if (checkedID == binding.radioBtnOtherAd.getId()) {
                // Khi chọn "Text nhập được"

            }
        });

    }
}