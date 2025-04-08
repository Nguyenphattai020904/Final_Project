package com.example.final_project.Products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.final_project.Fragments.CartFragment;
import com.example.final_project.Fragments.CartManager;
import com.example.final_project.Activity.MainActivity;
import com.example.final_project.R;
import com.squareup.picasso.Picasso;

public class ProductDetailFragment extends Fragment {
    private ImageView productImage;
    private TextView productName, productPrice, productBrand, productCategory, productIngredients, productNutrients, productMainCategory, discountTag;
    private Button addToCartButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        productImage = view.findViewById(R.id.detail_product_image);
        productName = view.findViewById(R.id.detail_product_name);
        productPrice = view.findViewById(R.id.detail_product_price);
        productBrand = view.findViewById(R.id.detail_product_brand);
        productCategory = view.findViewById(R.id.detail_product_category);
        productIngredients = view.findViewById(R.id.detail_product_ingredients);
        productNutrients = view.findViewById(R.id.detail_product_nutrients);
        productMainCategory = view.findViewById(R.id.detail_product_main_category);
        discountTag = view.findViewById(R.id.detail_discount_tag);
        addToCartButton = view.findViewById(R.id.add_to_cart_button);

        Product product = (Product) getArguments().getSerializable("product");
        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.format("%.0f VND", product.getFinalPrice()));
            productBrand.setText("Thương hiệu: " + (product.getBrand() != null ? product.getBrand() : "N/A"));
            productCategory.setText("Loại sản phẩm: " + (product.getCategory() != null ? product.getCategory() : "N/A"));
            productIngredients.setText("Thành phần: " + (product.getIngredients() != null ? product.getIngredients() : "N/A"));
            productNutrients.setText("Dinh dưỡng: " + (product.getNutrients() != null ? product.getNutrients() : "N/A"));
            productMainCategory.setText("Phân loại sản phẩm chính: " + (product.getMainCategory() != null ? product.getMainCategory() : "N/A"));

            if (product.getDiscount() != null && product.getDiscount() > 0) {
                discountTag.setVisibility(View.VISIBLE);
                discountTag.setText(String.format("-%d%%", Math.round(product.getDiscount())));
            } else {
                discountTag.setVisibility(View.GONE);
            }

            String imageUrl = product.getImages();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    if (imageUrl.startsWith("[")) {
                        String[] images = imageUrl.substring(1, imageUrl.length() - 1).split(",");
                        imageUrl = images[0].replace("\"", "").trim();
                    }
                    Picasso.get().load(imageUrl).into(productImage);
                } catch (Exception e) {
                    productImage.setImageResource(R.drawable.img);
                }
            } else {
                productImage.setImageResource(R.drawable.img);
            }

            addToCartButton.setOnClickListener(v -> {
                // Truyền Context vào CartManager.getInstance()
                CartManager.getInstance(getContext()).addToCart(new Product(product.getProductId(), product.getName(), product.getFinalPrice(), product.getImages()));
                Toast.makeText(getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();

                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    // Truyền Context vào CartManager.getInstance()
                    activity.updateCartBadge(CartManager.getInstance(getContext()).getCartSize());
                }

                CartFragment cartFragment = (CartFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CartFragment");
                if (cartFragment != null && cartFragment.isVisible()) {
                    cartFragment.updateCartFromManager();
                }
            });
        }

        return view;
    }
}