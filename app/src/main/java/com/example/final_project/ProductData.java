package com.example.final_project;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;

public class ProductData {
    public static List<Product> getFakeProducts(int numberOfProducts) {
        List<Product> products = new ArrayList<>();
        Faker faker = new Faker();

        String[] categories = {"Fresh Food", "Dry Food", "Beverages", "Spices"};

        for (String category : categories) {
            for (int i = 0; i < numberOfProducts; i++) {
                String name = faker.commerce().productName();
                double price = faker.number().randomDouble(2, 1, 100);
                String brand = faker.company().name();
                int quantity = 1000;
                String imageUrl = "https://via.placeholder.com/150";

                products.add(new Product(i + 1, name, price, category, brand, quantity, imageUrl));
            }
        }

        return products;
    }
}
