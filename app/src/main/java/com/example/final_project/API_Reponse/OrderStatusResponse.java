package com.example.final_project.API_Reponse;

import com.google.gson.annotations.SerializedName;

public class OrderStatusResponse {
    @SerializedName("payment_status")
    private String paymentStatus;
    @SerializedName("newOrderId")
    private Integer newOrderId;

    public String getPaymentStatus() { return paymentStatus; }
    public Integer getNewOrderId() { return newOrderId; }
}