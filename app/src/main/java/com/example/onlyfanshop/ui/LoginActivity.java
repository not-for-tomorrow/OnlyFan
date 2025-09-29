package com.example.onlyfanshop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.UserApi;
import com.example.onlyfanshop.model.ApiResponse;
import com.example.onlyfanshop.model.LoginRequest;
import com.example.onlyfanshop.model.UserDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private UserApi userApi;
    private EditText etUsername, etPassword;
    private Button btnLogin;

    private TextView tvForgotPassword, tvSignUp;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userApi = ApiClient.getClient().create(UserApi.class);
        etUsername = findViewById(R.id.edtUsername);
        etPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etUsername.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);

        btnLogin.setOnClickListener(v -> login());
    }

    private void checkInputFields() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        btnLogin.setEnabled(!username.isEmpty() && !password.isEmpty());
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập username và password", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);

        userApi.login(request).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                ApiResponse<UserDTO> apiResponse = response.body();

                if (apiResponse == null && response.errorBody() != null) {
                    apiResponse = parseErrorBody(response.errorBody(), UserDTO.class);
                }

                if (apiResponse != null) {
                    if (apiResponse.getStatusCode() == 200) {
                        UserDTO user = apiResponse.getData();
                        Toast.makeText(LoginActivity.this, "Welcome " + user.getUsername(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private <T> ApiResponse<T> parseErrorBody(ResponseBody errorBody, Class<T> dataClass) {
        try {
            return gson.fromJson(
                    errorBody.string(),
                    TypeToken.getParameterized(ApiResponse.class, dataClass).getType()
            );
        } catch (Exception e) {
            Log.e("LoginActivity", "parseErrorBody failed", e);
            return null;
        }
    }
}
