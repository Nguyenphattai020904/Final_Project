package com.example.final_project.API_Reponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderListResponse {
    private boolean success;
    private String message;
    private List<OrderItem> orders;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<OrderItem> getOrders() { return orders; }

    public static class OrderItem {
        @SerializedName("orderId")
        private String orderId;
        @SerializedName("orderDate")
        private String orderDate;
        @SerializedName("totalPrice")
        private double totalPrice;
        @SerializedName("firstProductImage")
        private String firstProductImage;
        @SerializedName("status")
        private String status; // Thêm trường status

        public String getOrderId() { return orderId; }
        public String getOrderDate() { return orderDate; }
        public double getTotalPrice() { return totalPrice; }
        public String getFirstProductImage() { return firstProductImage; }
        public String getStatus() { return status; } // Getter cho status
    }
}