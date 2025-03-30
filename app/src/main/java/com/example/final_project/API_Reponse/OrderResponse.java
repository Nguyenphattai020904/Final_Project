package com.example.final_project.API_Reponse;

public class OrderResponse {
    private String message;
    private int pendingOrderId;
    private double total_price;
    private String zaloPay_url;

    public String getMessage() {
        return message;
    }

    public int getPendingOrderId() {
        return pendingOrderId;
    }

    public double getTotalPrice() {
        return total_price;
    }

    public String getZaloPayUrl() {
        return zaloPay_url;
    }
}