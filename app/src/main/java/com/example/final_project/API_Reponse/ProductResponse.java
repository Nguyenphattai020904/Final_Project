package com.example.final_project.API_Reponse;

import com.example.final_project.Products.Product;
import java.util.List;

public class ProductResponse {
    private List<Product> products;
    private List<Product> bestSellers;

    public List<Product> getProducts() {
        return products;
    }

    public List<Product> getBestSellers() {
        return bestSellers;
    }
}