package com.example.final_project.API_Reponse;

import java.util.List;

public class OrderListResponse {
    private boolean success;
    private String message;
    private List<OrderItem> orders;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public static class OrderItem {
        private String orderId;
        private String orderDate;
        private double totalPrice;
        private String firstProductImage;

        public String getOrderId() {
            return orderId;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public String getFirstProductImage() {
            return firstProductImage;
        }
    }
}