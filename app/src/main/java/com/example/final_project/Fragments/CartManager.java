package com.example.final_project.Fragments;


import com.example.final_project.Products.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Product> cartItems = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product) {
        for (Product item : cartItems) {
            if (item.getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        product.setQuantity(1);
        cartItems.add(product);
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public int getCartSize() {
        return cartItems.size();
    }

    public void clearCart() {
        cartItems.clear();
    }
}
