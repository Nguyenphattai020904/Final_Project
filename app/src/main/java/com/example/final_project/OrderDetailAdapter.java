package com.example.final_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.API_Reponse.OrderDetailResponse;
import com.example.final_project.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderItemViewHolder> {

    private List<OrderDetailResponse.OrderItem> items;

    public OrderDetailAdapter(List<OrderDetailResponse.OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderDetailResponse.OrderItem item = items.get(position);
        holder.tvProductName.setText(item.getProductName()); // Hiển thị productName thay vì productId
        holder.tvQuantity.setText("Quantity: " + item.getQuantity());
        holder.tvPrice.setText("Price: " + formatPrice(item.getPrice()));

        // Load ảnh sản phẩm
        if (item.getImage() != null && !item.getImage().isEmpty()) {
            Picasso.get().load(item.getImage()).into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.img); // Ảnh mặc định nếu không có ảnh
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice; // Đổi tvProductId thành tvProductName
        ImageView ivProductImage;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName); // Cập nhật ID
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}