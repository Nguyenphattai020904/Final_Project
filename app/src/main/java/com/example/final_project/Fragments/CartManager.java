package com.example.final_project.Fragments;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.final_project.Products.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Product> cartItems;
    private static final String PREF_NAME = "CartPrefs";
    private SharedPreferences prefs;
    private Gson gson;
    private String userId;

    private CartManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        SharedPreferences userPrefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String newUserId = userPrefs.getString("userId", null);

        if (!isSameUserId(instance.userId, newUserId)) {
            instance.userId = newUserId;
            instance.loadCartItems();
        }
        return instance;
    }

    private static boolean isSameUserId(String id1, String id2) {
        if (id1 == null && id2 == null) return true;
        if (id1 == null || id2 == null) return false;
        return id1.equals(id2);
    }

    public void addToCart(Product product) {
        for (Product item : cartItems) {
            if (item.getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                saveCartItems();
                return;
            }
        }
        product.setQuantity(1);
        cartItems.add(product);
        saveCartItems();
    }

    // Thêm phương thức để cập nhật số lượng
    public void updateQuantity(Product product, int newQuantity) {
        for (Product item : cartItems) {
            if (item.getProductId() == product.getProductId()) {
                item.setQuantity(newQuantity);
                saveCartItems();
                return;
            }
        }
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public int getCartSize() {
        return cartItems.size();
    }

    public void clearCart() {
        cartItems.clear();
        saveCartItems();
    }

    public void removeItem(Product product) {
        cartItems.remove(product);
        saveCartItems();
    }

    private void loadCartItems() {
        cartItems.clear();
        if (userId == null) {
            return;
        }
        String cartKey = "CartItems_" + userId;
        String json = prefs.getString(cartKey, null);
        if (json != null) {
            Type type = new TypeToken<List<Product>>(){}.getType();
            cartItems = gson.fromJson(json, type);
        }
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
    }

    private void saveCartItems() {
        if (userId == null) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        String cartKey = "CartItems_" + userId;
        String json = gson.toJson(cartItems);
        editor.putString(cartKey, json);
        editor.apply();
    }

    public void resetCart() {
        cartItems.clear();
        userId = null;
    }
}