package com.example.final_project.API_Reponse;

import java.io.Serializable;

public class MentionedProduct implements Serializable {
    private String name;
    private int product_id;
    private String images;
    private String nutrients;
    private double price;
    private String brand;
    private String category;
    private String ingredients;
    private String main_category;
    private String detailLink;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getProductId() { return product_id; }
    public void setProductId(int product_id) { this.product_id = product_id; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getNutrients() { return nutrients; }
    public void setNutrients(String nutrients) { this.nutrients = nutrients; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getMainCategory() { return main_category; }
    public void setMainCategory(String main_category) { this.main_category = main_category; }
    public String getDetailLink() { return detailLink; }
    public void setDetailLink(String detailLink) { this.detailLink = detailLink; }
}