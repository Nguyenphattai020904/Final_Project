package com.example.final_project.API_Requests;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderRequest {
    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("total_price")
    private double totalPrice;

    @SerializedName("voucher_id")
    private int voucherId; // Thêm trường voucherId

    public OrderRequest(List<OrderItem> items, String paymentMethod, String name, String phone, String address, double totalPrice, int voucherId) {
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.totalPrice = totalPrice;
        this.voucherId = voucherId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public static class OrderItem {
        @SerializedName("product_id")
        private int productId;

        @SerializedName("quantity")
        private int quantity;

        public OrderItem(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public OrderItem(String productId, int quantity) {
            try {
                this.productId = Integer.parseInt(productId);
            } catch (NumberFormatException e) {
                this.productId = 0;
            }
            this.quantity = quantity;
        }

        public int getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "productId=" + productId +
                    ", quantity=" + quantity +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "items=" + items +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", totalPrice=" + totalPrice +
                ", voucherId=" + voucherId +
                '}';
    }
}