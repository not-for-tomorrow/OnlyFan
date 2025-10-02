package com.example.onlyfanshop.api;

import com.example.onlyfanshop.model.response.ApiResponse;
import com.example.onlyfanshop.model.Request.LoginRequest;
import com.example.onlyfanshop.model.Request.RegisterRequest;
import com.example.onlyfanshop.model.UserDTO;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    @POST("login/signin")
    Call<ApiResponse<UserDTO>> login(@Body LoginRequest request);
    @POST("login/register")
    Call<ApiResponse<Void>> register(@Body RegisterRequest request);
    @FormUrlEncoded
    @POST("login/send-otp")
    Call<ApiResponse<Void>> sendOtp(@Field("email") String email);

    @FormUrlEncoded
    @POST("login/verify-otp")
    Call<ApiResponse<Void>> verifyOtp(
            @Field("email") String email,
            @Field("otp") String otp
    );
    @GET("login/check-account")
    Call<Map<String, Boolean>> checkAccount(
            @Query("username") String username,
            @Query("email") String email
    );

    @FormUrlEncoded
    @POST("login/reset-password")
    Call<ApiResponse<Void>> resetPassword(
            @Field("email") String email,
            @Field("newPassword") String newPassword
    );
}
