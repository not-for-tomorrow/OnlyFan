package com.example.onlyfanshop.api;

import com.example.onlyfanshop.model.response.ApiResponse;
import com.example.onlyfanshop.model.ProductDetailDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProductApi {
    @GET("product/detail/{productId}")
    Call<ApiResponse<ProductDetailDTO>> getProductDetail(@Path("productId") int productId);
    
    @GET("product/test")
    Call<ApiResponse<String>> testConnection();
}




