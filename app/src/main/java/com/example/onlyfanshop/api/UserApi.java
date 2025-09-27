package com.example.onlyfanshop.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserApi {
    @GET("users")
    Call<List<String>> getUsers();

    @POST("users")
    Call<String> createUser(@Body String name);
}
