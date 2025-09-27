package com.example.onlyfanshop;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.UserApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private UserApi userApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserApi api = ApiClient.getClient().create(UserApi.class);

        api.getUsers().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    Log.d("API_TEST", "Kết nối OK: " + response.body());
                } else {
                    Log.e("API_TEST", "Lỗi Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("API_TEST", "Kết nối thất bại: " + t.getMessage());
            }
        });
    }
}

