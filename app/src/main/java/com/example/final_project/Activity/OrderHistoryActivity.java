package com.example.final_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderListResponse;
import com.example.final_project.Adapter.OrderHistoryAdapter;
import com.example.final_project.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderHistoryAdapter orderHistoryAdapter;
    private String userId;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter rỗng để tránh cảnh báo "No adapter attached"
        orderHistoryAdapter = new OrderHistoryAdapter(
                new ArrayList<>(),
                order -> {
                    Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                    intent.putExtra("orderId", order.getOrderId());
                    startActivity(intent);
                },
                orderId -> {
                    // Xử lý sự kiện "Mua lại"
                    Intent intent = new Intent(OrderHistoryActivity.this, CheckoutActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("isReorder", true);
                    startActivity(intent);
                }
        );
        recyclerViewOrders.setAdapter(orderHistoryAdapter);

        // Lấy userId và accessToken từ SharedPreferences
        userId = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", "");
        accessToken = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy userId. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<OrderListResponse> call = apiService.getUserOrders("Bearer " + accessToken, userId);

        call.enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(Call<OrderListResponse> call, Response<OrderListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    orderHistoryAdapter = new OrderHistoryAdapter(
                            response.body().getOrders(),
                            order -> {
                                Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                                intent.putExtra("orderId", order.getOrderId());
                                startActivity(intent);
                            },
                            orderId -> {
                                Intent intent = new Intent(OrderHistoryActivity.this, CheckoutActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("isReorder", true);
                                startActivity(intent);
                            }
                    );
                    recyclerViewOrders.setAdapter(orderHistoryAdapter);
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Không thể tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderListResponse> call, Throwable t) {
                Toast.makeText(OrderHistoryActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}