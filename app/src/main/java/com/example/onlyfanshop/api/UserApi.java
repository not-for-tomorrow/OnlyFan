package com.example.onlyfanshop.api;

import com.example.onlyfanshop.model.ApiResponse;
import com.example.onlyfanshop.model.LoginRequest;
import com.example.onlyfanshop.model.Request.RegisterRequest;
import com.example.onlyfanshop.model.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserApi {
    @POST("login/signin") // đường dẫn backend của bạn
    Call<ApiResponse<UserDTO>> login(@Body LoginRequest request);
    @POST("login/register")
    Call<ApiResponse<Void>> register(@Body RegisterRequest request);
}
