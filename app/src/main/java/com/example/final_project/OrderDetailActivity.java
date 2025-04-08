package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderDetailResponse;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvDetailOrderId, tvDetailOrderDate, tvDetailTotalPrice, tvDetailOrderStatus; // Thêm tvDetailOrderStatus
    private RecyclerView recyclerViewOrderProducts;
    private OrderDetailAdapter orderDetailAdapter;
    private Button btnReorder;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvDetailOrderId = findViewById(R.id.tvDetailOrderId);
        tvDetailTotalPrice = findViewById(R.id.tvDetailTotalPrice);
        tvDetailOrderStatus = findViewById(R.id.tvDetailOrderStatus); // Khởi tạo
        recyclerViewOrderProducts = findViewById(R.id.recyclerViewOrderProducts);
        btnReorder = findViewById(R.id.btnReorder);
        recyclerViewOrderProducts.setLayoutManager(new LinearLayoutManager(this));

        orderId = getIntent().getStringExtra("orderId");

        btnReorder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, CheckoutActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("isReorder", true);
            startActivity(intent);
        });

        loadOrderDetails(orderId);
    }

    private void loadOrderDetails(String orderId) {
        String token = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<OrderDetailResponse> call = apiService.getOrderDetails("Bearer " + token, orderId);

        call.enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    OrderDetailResponse.Order order = response.body().getOrder();

                    tvDetailOrderId.setText("Mã đơn hàng: " + order.getOrderId());
                    tvDetailTotalPrice.setText("Tổng giá: " + formatPrice(order.getTotalPrice()));
                    tvDetailOrderStatus.setText("Trạng thái: " + formatStatus(order.getStatus())); // Hiển thị status

                    orderDetailAdapter = new OrderDetailAdapter(order.getItems());
                    recyclerViewOrderProducts.setAdapter(orderDetailAdapter);
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Không thể tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
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