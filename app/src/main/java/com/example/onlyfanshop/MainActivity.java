package com.example.onlyfanshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

// Firebase imports for logout
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.ProductApi;
import com.example.onlyfanshop.api.UserApi;
import com.example.onlyfanshop.model.UserDTO;
import com.example.onlyfanshop.model.response.ApiResponse;
import com.example.onlyfanshop.ui.product.ProductDetailActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private UserApi userApi;
    private TextView textView;
    private EditText editProductId;
    private Button btnViewProduct;
    private Button btnTestApi;
    private Button btnLogout;
    
    // Firebase Authentication
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        initializeGoogleSignIn();
        
        UserDTO user = (UserDTO) getIntent().getSerializableExtra("user");
        textView = findViewById(R.id.textView);
        
        // Xử lý trường hợp user null
        if (user != null && user.getUsername() != null) {
            textView.setText("Welcome " + user.getUsername());
        } else {
            textView.setText("Welcome to OnlyFanshop!");
        }
        userApi = ApiClient.getClient().create(UserApi.class);
        
        editProductId = findViewById(R.id.editProductId);
        
        btnViewProduct = findViewById(R.id.btnViewProduct);
        btnViewProduct.setOnClickListener(v -> {
            // Get product ID from input and open product detail
            openProductDetail();
        });

        btnTestApi = findViewById(R.id.btnTestApi);
        btnTestApi.setOnClickListener(v -> {
            // Test API only
            testApiOnly();
        });
        
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void openProductDetail() {
        String productIdText = editProductId.getText().toString().trim();
        
        if (productIdText.isEmpty()) {
            Toast.makeText(this, "Please enter a Product ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int productId = Integer.parseInt(productIdText);
            Log.d("MainActivity", "Opening product detail for ID: " + productId);
            
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId);
            startActivity(intent);
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void testApiConnection() {
        Log.d("MainActivity", "Testing API connection...");
        Log.d("MainActivity", "Base URL: " + ApiClient.getClient().baseUrl());
        
        ProductApi api = ApiClient.getClient().create(ProductApi.class);
        
        // First test basic connectivity
        Call<ApiResponse<String>> testCall = api.testConnection();
        testCall.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                Log.d("MainActivity", "Response code: " + response.code());
                Log.d("MainActivity", "Response body: " + response.body());
                Log.d("MainActivity", "Response headers: " + response.headers());
                
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getData();
                    Toast.makeText(MainActivity.this, "API Connected: " + message, Toast.LENGTH_SHORT).show();
                    // Now open product detail
                    Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                    intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, 1);
                    startActivity(intent);
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody().string();
                    } catch (Exception e) {
                        errorBody = "Could not read error body";
                    }
                    Log.e("MainActivity", "Error response: " + errorBody);
                    Toast.makeText(MainActivity.this, "API Error: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e("MainActivity", "API connection failed: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Cannot connect to server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void testApiOnly() {
        Log.d("MainActivity", "Testing API only...");
        
        // Test with raw HTTP first
        testRawHttp();
    }

    private void testRawHttp() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("http://10.0.2.2:8080/product/test");
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                Log.d("MainActivity", "Raw HTTP Response Code: " + responseCode);
                
                String responseBody;
                if (responseCode >= 200 && responseCode < 300) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    responseBody = response.toString();
                } else {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getErrorStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    responseBody = response.toString();
                }
                
                Log.d("MainActivity", "Raw HTTP Response Body: " + responseBody);
                
                runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        Toast.makeText(MainActivity.this, "Raw HTTP Success: " + responseBody, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Raw HTTP Error " + responseCode + ": " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
                
            } catch (Exception e) {
                Log.e("MainActivity", "Raw HTTP failed: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Raw HTTP Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    // Initialize Google Sign-In for logout
    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("328191492825-8iket64hs1nr651gn0jnb19js7aimj10.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    
    // Logout method
    private void logout() {
        Log.d("Logout", "Starting logout process");
        
        // Sign out from Firebase
        mAuth.signOut();
        
        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Log.d("Logout", "Google sign out completed");
            
            // Show logout success message
            Toast.makeText(MainActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            
            // Navigate back to LoginActivity
            Intent intent = new Intent(MainActivity.this, com.example.onlyfanshop.ui.login.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}

