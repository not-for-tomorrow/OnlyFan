package com.example.onlyfanshop.api;

import com.example.onlyfanshop.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProfileApi {
    @GET("users/getUser")
    Call<UserResponse> getUser();
}