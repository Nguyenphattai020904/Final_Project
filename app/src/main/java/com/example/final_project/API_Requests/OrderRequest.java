package com.example.final_project.API_Requests;

import java.util.List;

public class OrderRequest {
    private int user_id;
    private List<OrderItem> items;
    private String payment_method;
    private String name;
    private String phone;
    private String address;

    public OrderRequest(int user_id, List<OrderItem> items, String payment_method, String name, String phone, String address) {
        this.user_id = user_id;
        this.items = items;
        this.payment_method = payment_method;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public static class OrderItem {
        private int product_id;
        private int quantity;

        public OrderItem(int product_id, int quantity) {
            this.product_id = product_id;
            this.quantity = quantity;
        }
    }
}