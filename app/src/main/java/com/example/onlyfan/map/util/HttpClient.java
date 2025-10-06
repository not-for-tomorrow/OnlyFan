package com.example.onlyfan.map.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.*;

public class HttpClient {

    private static final OkHttpClient client = new OkHttpClient();

    public interface ResponseCallback {
        void onSuccess(String body);
        void onError(Exception e);
    }

    public static void get(String url, ResponseCallback cb) {
        Request req = new Request.Builder().url(url).get().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError(e); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    cb.onError(new IOException("HTTP " + response.code()));
                    return;
                }
                cb.onSuccess(response.body().string());
            }
        });
    }

    public static void postJson(String url, String bodyJson, String apiKey, ResponseCallback cb) {
        MediaType JSON = MediaType.parse("application/json");
        RequestBody b = RequestBody.create(bodyJson, JSON);
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(b)
                .build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError(e); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    cb.onError(new IOException("HTTP " + response.code()));
                    return;
                }
                cb.onSuccess(response.body().string());
            }
        });
    }

    public static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}