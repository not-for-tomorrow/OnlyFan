package com.example.onlyfanshop.model.Request;

public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String phoneNumber;
    private String address;

    public RegisterRequest(String username, String password, String confirmPassword, String email, String phoneNumber, String address) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
