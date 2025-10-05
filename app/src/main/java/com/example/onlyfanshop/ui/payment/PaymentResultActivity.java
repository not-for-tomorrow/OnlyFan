package com.example.onlyfanshop.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlyfanshop.R;
import com.example.onlyfanshop.MainActivity;

public class PaymentResultActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT = "payment_result"; // "success" hoặc "failed"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String result = getIntent().getStringExtra(EXTRA_RESULT);

        if ("success".equals(result)) {
            setContentView(R.layout.activity_payment_success);
        } else {
            setContentView(R.layout.activity_payment_failed);
        }

        Button btnGoHome = findViewById(R.id.btnGoHome);
        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
