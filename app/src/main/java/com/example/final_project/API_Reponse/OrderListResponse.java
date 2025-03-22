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
        private String productId;
        private int quantity;
        private String status;
        private String orderDate;

        public String getOrderId() {
            return orderId;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getStatus() {
            return status;
        }

        public String getOrderDate() {
            return orderDate;
        }
    }
}
