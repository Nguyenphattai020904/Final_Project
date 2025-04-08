package com.example.final_project.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_project.API_Reponse.OrderListResponse;
import com.example.final_project.R;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    private List<OrderListResponse.OrderItem> orders;
    private final OnOrderClickListener listener;
    private final OnReorderClickListener reorderListener;

    public OrderHistoryAdapter(List<OrderListResponse.OrderItem> orders, OnOrderClickListener listener, OnReorderClickListener reorderListener) {
        this.orders = orders;
        this.listener = listener;
        this.reorderListener = reorderListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderListResponse.OrderItem order = orders.get(position);
        holder.tvOrderId.setText("Mã đơn hàng: " + order.getOrderId());
        holder.tvOrderDate.setText("Ngày: " + order.getOrderDate());
        holder.tvOrderTotal.setText(String.valueOf(order.getTotalPrice()));
        holder.tvOrderStatus.setText("Trạng thái: " + formatStatus(order.getStatus())); // Hiển thị status

        String imageUrl = order.getFirstProductImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(holder.ivFirstProduct);
        } else {
            holder.ivFirstProduct.setImageResource(R.drawable.img);
        }

        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
        holder.btnReorder.setOnClickListener(v -> reorderListener.onReorderClick(order.getOrderId()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvOrderStatus; // Thêm tvOrderStatus
        ImageView ivFirstProduct;
        Button btnReorder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus); // Khởi tạo
            ivFirstProduct = itemView.findViewById(R.id.ivFirstProduct);
            btnReorder = itemView.findViewById(R.id.btnReorder);
        }
    }

    public interface OnOrderClickListener {
        void onOrderClick(OrderListResponse.OrderItem order);
    }

    public interface OnReorderClickListener {
        void onReorderClick(String orderId);
    }

    private String formatStatus(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "pending": return "Đang xử lý";
            case "confirmed": return "Đã xác nhận";
            case "packing": return "Đang đóng gói";
            case "shipping": return "Đang giao hàng";
            case "delivered": return "Đã giao hàng";
            default: return status;
        }
    }
}