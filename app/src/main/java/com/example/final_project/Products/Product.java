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
    private String images; // URL hoặc chuỗi JSON chứa danh sách ảnh
    private String environment_data;
    private double price;
    private int quantity; // Số lượng trong giỏ hàng hoặc tồn kho
    private String created_at;
    private String updated_at;
    private String main_category;

    // Constructor cơ bản cho danh sách sản phẩm
    public Product(int product_id, String name, double price, String images) {
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.images = images;
        this.quantity = 0; // Khởi tạo mặc định là 0
    }

    // Getters
    public int getProductId() { return product_id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
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