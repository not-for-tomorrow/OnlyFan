package com.example.onlyfanshop.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
            Log.d("AuthInterceptor", "Adding token: " + token);
        }

        Request modifiedRequest = builder.build();

        // Log toàn bộ header trước khi gửi
        Log.d("AuthInterceptor", "Request headers: " + modifiedRequest.headers());

        return chain.proceed(modifiedRequest);
    }

}
