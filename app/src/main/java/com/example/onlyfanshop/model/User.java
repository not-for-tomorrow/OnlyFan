package com.example.onlyfanshop.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("userID")
    private int userID;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("role")
    private String role;

    @SerializedName("authProvider")
    private String authProvider;

    @SerializedName("token")
    private String token;

    public int getUserID() { return userID; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public String getAuthProvider() { return authProvider; }
    public String getToken() { return token; }
}