package com.example.onlyfanshop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.UserApi;
import com.example.onlyfanshop.model.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {
    private UserApi userApi;
    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        userApi = ApiClient.getClient().create(UserApi.class);
        String email = getIntent().getStringExtra("email");
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(v -> {
            String password = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            resetPassword(email, password, confirmPassword);
        });



    }

    private void resetPassword(String email, String newPassword, String confirmPassword) {
        if (newPassword.isEmpty()) {
            edtConfirmPassword.setBackgroundResource(R.drawable.edittext_error);
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }
        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setBackgroundResource(R.drawable.edittext_error);
            edtConfirmPassword.setError("Vui lòng nhập xác nhận mật khẩu");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setBackgroundResource(R.drawable.edittext_error);
            edtConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }
        Call<ApiResponse<Void>> call = userApi.resetPassword(email, newPassword);
        call.enqueue(new Callback<ApiResponse<Void>>() {
                         @Override
                         public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                             if (response.isSuccessful() && response.body() != null) {
                                 ApiResponse<Void> apiResponse = response.body();
                                 Toast.makeText(ResetPasswordActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                 Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                 startActivity(intent);
                                 finish(); // đóng ResetPassActivity
                             } else {
                                 Toast.makeText(ResetPasswordActivity.this, "Không thể đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                             }
                         }

                         @Override
                         public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                             Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     }
        );
    }
}