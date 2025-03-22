package com.example.final_project.API_Reponse;

public class OrderResponse {
    private String message;
    private int orderId;
    private double total_price;
    private String zaloPay_url;

    public String getMessage() {
        return message;
    }

    public int getOrderId() {
        return orderId;
    }

    public double getTotalPrice() {
        return total_price;
    }

    public String getZaloPayUrl() {
        return zaloPay_url;
    }
}