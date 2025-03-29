package com.example.final_project.API_Reponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("order")
    private Order order;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public static class Order {
        @SerializedName("orderId")
        private String orderId;

        @SerializedName("orderDate")
        private String orderDate;

        @SerializedName("totalPrice")
        private double totalPrice;

        @SerializedName("items")
        private List<OrderItem> items;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "orderId='" + orderId + '\'' +
                    ", orderDate='" + orderDate + '\'' +
                    ", totalPrice=" + totalPrice +
                    ", items=" + items +
                    '}';
        }
    }

    public static class OrderItem {
        @SerializedName("productId")
        private String productId;

        @SerializedName("productName")
        private String productName;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("price")
        private double price;

        @SerializedName("image")
        private String image;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", price=" + price +
                    ", image='" + image + '\'' +
                    '}';
        }
    }
}