package com.example.onlyfanshop.model;

import com.google.gson.annotations.SerializedName;

public class PaymentDTO {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("paymentUrl")
    private String paymentUrl;

    // Getters
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }
}
