package com.example.final_project.Products;

import java.io.Serializable;

public class Product implements Serializable {
    private int product_id;
    private String barcode;
    private String name;
    private String brand;
    private String category;
    private String ingredients;
    private String nutrients;
    private String additives;
    private String eco_labels;
    private String weight_volume;
    private String origin;
    private String images;
    private String environment_data;
    private double price;
    private Double discount; // Có thể null
    private double final_price;
    private int quantity;
    private String created_at;
    private String updated_at;
    private String main_category;

    // Constructor cơ bản (dùng chung cho danh sách sản phẩm và giỏ hàng)
    public Product(int product_id, String name, double priceOrFinalPrice, String images) {
        this.product_id = product_id;
        this.name = name;
        this.price = priceOrFinalPrice; // Giá gốc ban đầu
        this.final_price = priceOrFinalPrice; // Mặc định final_price = price nếu không có discount
        this.images = images;
        this.quantity = 0;
    }

    // Constructor đầy đủ từ API
    public Product(int product_id, String name, double price, Double discount, double final_price, String images, String main_category) {
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.final_price = final_price;
        this.images = images;
        this.main_category = main_category;
        this.quantity = 0;
    }

    // Getters
    public int getProductId() { return product_id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public Double getDiscount() { return discount; }
    public double getFinalPrice() { return final_price; }
    public String getImages() { return images; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public String getIngredients() { return ingredients; }
    public String getNutrients() { return nutrients; }
    public String getAdditives() { return additives; }
    public String getEcoLabels() { return eco_labels; }
    public String getWeightVolume() { return weight_volume; }
    public String getOrigin() { return origin; }
    public String getEnvironmentData() { return environment_data; }
    public int getQuantity() { return quantity; }
    public String getCreatedAt() { return created_at; }
    public String getUpdatedAt() { return updated_at; }
    public String getMainCategory() { return main_category; }

    // Setter cho quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}