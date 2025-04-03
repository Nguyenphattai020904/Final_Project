package com.example.final_project.Products;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.Fragments.CartFragment;
import com.example.final_project.Fragments.CartManager;
import com.example.final_project.MainActivity;
import com.example.final_project.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;
    Button addToCartButton;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());

        // Kiểm tra nếu có giảm giá
        if (product.getDiscount() != null && product.getDiscount() > 0) {
            holder.originalPrice.setVisibility(View.VISIBLE);
            holder.originalPrice.setText(String.format("%.0f VND", product.getPrice()));
            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Gạch ngang
            holder.productPrice.setText(String.format("%.0f VND", product.getFinalPrice()));
            holder.discountTag.setVisibility(View.VISIBLE);
            holder.discountTag.setText(String.format("-%d%%", Math.round(product.getDiscount())));
        } else {
            holder.originalPrice.setVisibility(View.GONE);
            holder.productPrice.setText(String.format("%.0f VND", product.getPrice()));
            holder.discountTag.setVisibility(View.GONE);
        }

        String imageUrl = product.getImages();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                if (imageUrl.startsWith("[")) {
                    String[] images = imageUrl.substring(1, imageUrl.length() - 1).split(",");
                    imageUrl = images[0].replace("\"", "").trim();
                }
                Picasso.get().load(imageUrl).into(holder.productImage);
            } catch (Exception e) {
                holder.productImage.setImageResource(R.drawable.img);
            }
        } else {
            holder.productImage.setImageResource(R.drawable.img);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        holder.addToCartButton.setOnClickListener(v -> {
            CartManager.getInstance(context).addToCart(new Product(product.getProductId(), product.getName(), product.getFinalPrice(), product.getImages()));
            Toast.makeText(context, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();

            if (context instanceof MainActivity) {
                ((MainActivity) context).updateCartBadge(CartManager.getInstance(context).getCartSize());
            }

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                CartFragment cartFragment = (CartFragment) activity.getSupportFragmentManager().findFragmentByTag("CartFragment");
                if (cartFragment != null && cartFragment.isVisible()) {
                    cartFragment.updateCartFromManager();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, discountTag, originalPrice;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            discountTag = itemView.findViewById(R.id.discount_tag);
            originalPrice = itemView.findViewById(R.id.original_price);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }
    }
}