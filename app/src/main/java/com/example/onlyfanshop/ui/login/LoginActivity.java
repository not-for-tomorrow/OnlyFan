package com.example.onlyfanshop.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
// Animation imports removed since logo is now static
// import android.view.animation.Animation;
// import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// Activity Result API imports
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlyfanshop.MainActivity;
import com.example.onlyfanshop.R;
import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.UserApi;
import com.example.onlyfanshop.model.response.ApiResponse;
import com.example.onlyfanshop.model.Request.LoginRequest;
import com.example.onlyfanshop.model.UserDTO;
import com.example.onlyfanshop.ultils.Validation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// Firebase imports
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.OAuthProvider;

// Google Sign-In imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

// Facebook Login imports
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private UserApi userApi;
    private EditText etUsername, etPassword;
    private Button btnLogin, btnLoginGoogle, btnLoginFacebook;
    private ImageView logoFan;

    private TextView tvForgotPassword, tvSignUp;
    private final Gson gson = new Gson();
    
    // Firebase Authentication
    private FirebaseAuth mAuth;
    
    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    
    // Facebook Login
    private CallbackManager mCallbackManager;
    private ActivityResultLauncher<Intent> facebookLoginLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize Google Sign-In
        initializeGoogleSignIn();
        
        // Initialize Facebook Login
        initializeFacebookLogin();
        
        // Initialize Activity Result Launcher for Google Sign-In
        initializeGoogleSignInLauncher();
        
        // Initialize Activity Result Launcher for Facebook Login
        initializeFacebookLoginLauncher();

        userApi = ApiClient.getClient().create(UserApi.class);
        etUsername = findViewById(R.id.edtUsername);
        etPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);
        logoFan = findViewById(R.id.logoFan);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        
        // Logo is now fixed (no rotation)
        // startFanAnimation(); // Commented out to keep logo static
        
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotActivity.class);
            startActivity(intent);
        });
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Set click listeners for social login buttons
        btnLoginGoogle.setOnClickListener(v -> signInWithGoogle());
        btnLoginFacebook.setOnClickListener(v -> signInWithFacebook());
        
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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
        Log.d("LoginActivityLog", "login() called"+username+password);
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập username và password", Toast.LENGTH_SHORT).show();
            return;
        }
        // Note: Username can be either username or email, so we don't validate email format here

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
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user", user);
                        startActivity(intent);
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
    
    // Initialize Google Sign-In
    private void initializeGoogleSignIn() {
        // Sử dụng client ID từ google-services.json
        // Lấy client ID từ Firebase Console > Project Settings > General > Web API Key
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("328191492825-8iket64hs1nr651gn0jnb19js7aimj10.apps.googleusercontent.com") // Web client ID từ Firebase Console
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d("GoogleSignIn", "Google Sign-In initialized with client ID: 328191492825-8iket64hs1nr651gn0jnb19js7aimj10.apps.googleusercontent.com");
    }
    
    // Initialize Facebook Login
    private void initializeFacebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookLogin", "Facebook login successful");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FacebookLogin", "Facebook login cancelled");
                Toast.makeText(LoginActivity.this, "Facebook login cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("FacebookLogin", "Facebook login error: " + exception.getMessage());
                Toast.makeText(LoginActivity.this, "Facebook login error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Initialize Google Sign-In Launcher
    private void initializeGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("GoogleSignIn", "Google Sign-In result: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Log.d("GoogleSignIn", "Processing Google Sign-In result");
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                Log.d("GoogleSignIn", "Google sign in successful");
                                firebaseAuthWithGoogle(account.getIdToken());
                            } else {
                                Log.e("GoogleSignIn", "Google account is null");
                                Toast.makeText(this, "Google sign in failed: Account is null", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            Log.e("GoogleSignIn", "Google sign in failed: " + e.getMessage() + " (Code: " + e.getStatusCode() + ")");
                            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("GoogleSignIn", "Google Sign-In data is null");
                        Toast.makeText(this, "Google Sign-In data is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("GoogleSignIn", "Google sign in cancelled by user");
                    Toast.makeText(this, "Google sign in cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    // Google Sign-In method
    private void signInWithGoogle() {
        Log.d("GoogleSignIn", "Starting Google Sign-In process");
        try {
            // Đảm bảo sign out trước để hiển thị account picker
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Log.d("GoogleSignIn", "Signed out from previous session");
                
                // Sau khi sign out, launch sign in intent
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                if (signInIntent != null) {
                    Log.d("GoogleSignIn", "Launching Google Sign-In intent with account picker");
                    googleSignInLauncher.launch(signInIntent);
                } else {
                    Log.e("GoogleSignIn", "Google Sign-In intent is null");
                    Toast.makeText(this, "Google Sign-In không khả dụng", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("GoogleSignIn", "Error starting Google Sign-In: " + e.getMessage());
            Toast.makeText(this, "Lỗi Google Sign-In: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Initialize Facebook Login Launcher
    private void initializeFacebookLoginLauncher() {
        facebookLoginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Facebook login result will be handled by CallbackManager
                // This launcher is mainly for compatibility
                Log.d("FacebookLogin", "Facebook login launcher result: " + result.getResultCode());
            }
        );
    }
    
    // Facebook Sign-In method
    private void signInWithFacebook() {
        // Use the new approach with proper error handling
        LoginManager.getInstance().logInWithReadPermissions(
            this, 
            java.util.Arrays.asList("email", "public_profile")
        );
    }
    
    // Handle Facebook login result (Google Sign-In now uses Activity Result API)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Only handle Facebook login result
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    
    // Firebase authentication with Google
    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            Log.e("GoogleAuth", "ID token is null or empty");
            Toast.makeText(this, "Google authentication failed: Invalid token", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d("GoogleAuth", "Firebase authentication with Google successful");
                    FirebaseUser user = mAuth.getCurrentUser();
                    handleSuccessfulLogin(user);
                } else {
                    Log.e("GoogleAuth", "Firebase authentication with Google failed: " + task.getException().getMessage());
                    Toast.makeText(LoginActivity.this, "Google authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    // Handle Facebook access token
    private void handleFacebookAccessToken(AccessToken token) {
        if (token == null || token.getToken() == null || token.getToken().isEmpty()) {
            Log.e("FacebookAuth", "Facebook access token is null or empty");
            Toast.makeText(this, "Facebook authentication failed: Invalid token", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d("FacebookAuth", "Firebase authentication with Facebook successful");
                    FirebaseUser user = mAuth.getCurrentUser();
                    handleSuccessfulLogin(user);
                } else {
                    Log.e("FacebookAuth", "Firebase authentication with Facebook failed: " + task.getException().getMessage());
                    Toast.makeText(LoginActivity.this, "Facebook authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    // Handle successful login
    private void handleSuccessfulLogin(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            
            // Tạo UserDTO từ Firebase user
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(user.getDisplayName());
            userDTO.setEmail(user.getEmail());
            
            // Chuyển đến MainActivity với thông tin user
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user", userDTO);
            startActivity(intent);
            finish();
        }
    }
    
    // Logo is now fixed (no rotation animation)
    // Animation methods are commented out to keep logo static
    
    /*
    // Start fan rotation animation
    private void startFanAnimation() {
        if (logoFan != null) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_fan_slow);
            logoFan.startAnimation(rotateAnimation);
            Log.d("FanAnimation", "Fan rotation animation started");
        }
    }
    
    // Stop fan rotation animation
    private void stopFanAnimation() {
        if (logoFan != null) {
            logoFan.clearAnimation();
            Log.d("FanAnimation", "Fan rotation animation stopped");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop animation when activity is destroyed
        stopFanAnimation();
    }
    */
    
}
