package com.example.onlyfanshop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.UserApi;
import com.example.onlyfanshop.model.UserDTO;

public class MainActivity extends AppCompatActivity {
    private UserApi userApi;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserDTO user = (UserDTO) getIntent().getSerializableExtra("user");
        textView = findViewById(R.id.textView);
        textView.setText("Welcome " + user.getUsername());
        userApi = ApiClient.getClient().create(UserApi.class);
    }
}

