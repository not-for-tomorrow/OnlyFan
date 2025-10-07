package com.example.onlyfanshop.api;

import com.example.onlyfanshop.model.CartItemDTO;
import com.example.onlyfanshop.model.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CartItemApi {
    @GET("cartItem/showCartItem")
    Call<ApiResponse<List<CartItemDTO>>> getCartItem(@Field("username") String username);
}
