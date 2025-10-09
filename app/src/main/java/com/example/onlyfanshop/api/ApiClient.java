package com.example.onlyfanshop.api;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client với 2 loại:
 * - Public: không cần token
 * - Private: có chèn token qua AuthInterceptor hoặc setAuthToken(...)
 *
 * Không phụ thuộc BuildConfig. Dùng setDebugLoggingEnabled(...) để bật tắt logging.
 */
public final class ApiClient {

    private ApiClient() {}

    private static volatile Retrofit retrofitPublic;
    private static volatile Retrofit retrofitPrivate;

    private static volatile OkHttpClient okHttpPublic;
    private static volatile OkHttpClient okHttpPrivate;

    // Mặc định cho Android Emulator
    private static volatile String BASE_URL = "http://10.0.2.2:8080/";
    private static volatile String authToken;

    // Bật/tắt logging mức BODY cho debug
    private static volatile boolean debugLoggingEnabled = false;

    // =========================
    // Config helpers
    // =========================
    public static void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) return;
        BASE_URL = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        reset();
    }

    public static void setAuthToken(@Nullable String token) {
        authToken = token;
        synchronized (ApiClient.class) {
            retrofitPrivate = null;
            okHttpPrivate = null;
        }
    }

    public static void clearAuthToken() {
        setAuthToken(null);
    }

    public static void setDebugLoggingEnabled(boolean enabled) {
        debugLoggingEnabled = enabled;
        synchronized (ApiClient.class) {
            okHttpPublic = null;
            okHttpPrivate = null;
            retrofitPublic = null;
            retrofitPrivate = null;
        }
    }

    public static void reset() {
        synchronized (ApiClient.class) {
            retrofitPublic = null;
            retrofitPrivate = null;
            okHttpPublic = null;
            okHttpPrivate = null;
        }
    }

    // =========================
    // Retrofit getters
    // =========================
    // API không cần token
    public static Retrofit getPublicClient() {
        if (retrofitPublic == null) {
            synchronized (ApiClient.class) {
                if (retrofitPublic == null) {
                    if (okHttpPublic == null) {
                        okHttpPublic = buildOkHttp(false, null);
                    }
                    retrofitPublic = buildRetrofit(okHttpPublic);
                }
            }
        }
        return retrofitPublic;
    }

    // API cần token
    public static Retrofit getPrivateClient(Context context) {
        if (retrofitPrivate == null) {
            synchronized (ApiClient.class) {
                if (retrofitPrivate == null) {
                    if (okHttpPrivate == null) {
                        okHttpPrivate = buildOkHttp(true, context);
                    }
                    retrofitPrivate = buildRetrofit(okHttpPrivate);
                }
            }
        }
        return retrofitPrivate;
    }

    // =========================
    // Builders
    // =========================
    private static Retrofit buildRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .client(client)
                .build();
    }

    private static Gson buildGson() {
        return new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
    }

    private static OkHttpClient buildOkHttp(boolean withAuth, @Nullable Context context) {
        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        // Logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(debugLoggingEnabled ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.BASIC);
        b.addInterceptor(logging);

        // Default headers
        b.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder()
                    .header("Accept", "application/json");
            // Chỉ set Content-Type cho request có body
            String method = original.method();
            if ("POST".equalsIgnoreCase(method)
                    || "PUT".equalsIgnoreCase(method)
                    || "PATCH".equalsIgnoreCase(method)) {
                builder.header("Content-Type", "application/json");
            }
            return chain.proceed(builder.build());
        });

        // Authorization
        if (withAuth) {
            if (authToken != null && !authToken.isEmpty()) {
                b.addInterceptor(chain -> {
                    Request req = chain.request().newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .build();
                    return chain.proceed(req);
                });
            } else if (context != null) {
                // Để nguyên theo code của bạn: tự lấy token trong AuthInterceptor
                b.addInterceptor(new AuthInterceptor(context));
            }
        }

        return b.build();
    }
    // Thêm vào bên trong class ApiClient:
    public static retrofit2.Retrofit getInstance() {
        // Trả về public client mặc định để tương thích các chỗ gọi cũ
        return getPublicClient();
    }
}