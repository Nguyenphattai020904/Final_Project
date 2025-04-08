package com.example.final_project.Adapter;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.Fragments.CartFragment;
import com.example.final_project.Fragments.CartManager;
import com.example.final_project.Products.Product;
import com.example.final_project.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Product> cartItems;
    private OnQuantityChangeListener listener;
    private List<Product> selectedItems;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();

        void onSelectionChanged();
    }

    public CartAdapter(Context context, List<Product> cartItems, OnQuantityChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.selectedItems = new ArrayList<>();
        Log.d("CartAdapter", "Initialized with " + cartItems.size() + " items");
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_card_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartItems.get(position);
        Log.d("CartAdapter", "Binding item at position " + position + ": " + product.getName());

        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format("%.0f VND", product.getFinalPrice()));
        holder.quantityText.setText(String.valueOf(product.getQuantity()));

        String imageUrl = product.getImages();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                if (imageUrl.startsWith("[")) {
                    String[] images = imageUrl.substring(1, imageUrl.length() - 1).split(",");
                    imageUrl = images[0].replace("\"", "").trim();
                }
                Picasso.get().load(imageUrl).into(holder.productImage);
            } catch (Exception e) {
                Log.e("CartAdapter", "Error loading image: " + e.getMessage());
                holder.productImage.setImageResource(R.drawable.ic_cart);
            }
        } else {
            holder.productImage.setImageResource(R.drawable.ic_cart);
        }

        // Xử lý checkbox
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedItems.contains(product));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedItems.contains(product)) selectedItems.add(product);
            } else {
                selectedItems.remove(product);
            }
            // Cập nhật trạng thái của checkbox Select All
            if (listener instanceof CartFragment) {
                ((CartFragment) listener).updateSelectAllCheckBox();
            }
        });

        // Xử lý nút giảm số lượng
        holder.decreaseButton.setOnClickListener(v -> {
            int currentQuantity = product.getQuantity();
            if (currentQuantity > 1) {
                product.setQuantity(currentQuantity - 1);
                holder.quantityText.setText(String.valueOf(product.getQuantity()));
                CartManager.getInstance(context).updateQuantity(product, product.getQuantity());
                if (listener != null) listener.onQuantityChanged();
            }
        });

        // Xử lý nút tăng số lượng
        holder.increaseButton.setOnClickListener(v -> {
            int currentQuantity = product.getQuantity();
            product.setQuantity(currentQuantity + 1);
            holder.quantityText.setText(String.valueOf(product.getQuantity()));
            CartManager.getInstance(context).updateQuantity(product, product.getQuantity());
            if (listener != null) listener.onQuantityChanged();
        });

        // Xử lý nút xóa
        holder.deleteButton.setOnClickListener(v -> {
            CartManager.getInstance(context).removeItem(product);
            selectedItems.remove(product);
            notifyDataSetChanged();
            if (listener != null) listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        int count = cartItems.size();
        Log.d("CartAdapter", "getItemCount: " + count);
        return count;
    }

    public List<Product> getSelectedItems() {
        return selectedItems;
    }

    // Phương thức để chọn hoặc bỏ chọn tất cả sản phẩm
    public void selectAll(boolean isChecked) {
        selectedItems.clear();
        if (isChecked) {
            selectedItems.addAll(cartItems);
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView productImage;
        TextView productName, productPrice, quantityText;
        ImageButton decreaseButton, increaseButton, deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_select);
            productImage = itemView.findViewById(R.id.imgProduct);
            productName = itemView.findViewById(R.id.txtNameProduct);
            productPrice = itemView.findViewById(R.id.txtPriceProduct);
            quantityText = itemView.findViewById(R.id.txtQuantity);
            decreaseButton = itemView.findViewById(R.id.btnDecrease);
            increaseButton = itemView.findViewById(R.id.btnIncrease);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    public static class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public ItemSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;
            outRect.top = spacing;
        }
    }
}