package com.example.onlyfanshop.model.response;

import com.example.onlyfanshop.model.User;
import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private User data;

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public User getData() { return data; }
}