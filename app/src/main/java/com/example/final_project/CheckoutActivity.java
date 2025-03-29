package com.example.final_project;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderDetailResponse;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Requests.OrderRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText edtFullName, edtAddress, edtPhoneNumber;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbZaloPay;
    private Button btnConfirmOrder, btnCancelOrder;
    private LinearLayout orderItemsContainer;
    private TextView txtTotalCost;
    private ProgressDialog progressDialog;
    private String orderId;
    private boolean isReorder;
    private List<OrderDetailResponse.OrderItem> reorderItems;
    private double totalPrice;
    private boolean isItemsLoaded = false; // Biến để kiểm tra xem dữ liệu đã được tải chưa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        edtFullName = findViewById(R.id.edt_full_name);
        edtAddress = findViewById(R.id.edt_address);
        edtPhoneNumber = findViewById(R.id.edt_phone_number);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        rbCOD = findViewById(R.id.rb_cod);
        rbZaloPay = findViewById(R.id.rb_zalo_pay);
        btnConfirmOrder = findViewById(R.id.btn_confirm_order);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        orderItemsContainer = findViewById(R.id.order_items_container);
        txtTotalCost = findViewById(R.id.txt_total_cost);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý đơn hàng...");
        progressDialog.setCancelable(false);

        // Vô hiệu hóa nút "Xác nhận" cho đến khi dữ liệu được tải
        btnConfirmOrder.setEnabled(false);

        // Lấy dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        isReorder = getIntent().getBooleanExtra("isReorder", false);

        if (isReorder) {
            loadReorderItems();
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Chức năng này chỉ hỗ trợ mua lại đơn hàng", Toast.LENGTH_SHORT).show());
            finish();
        }

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
        btnCancelOrder.setOnClickListener(v -> finish());
    }

    private void loadReorderItems() {
        String token = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");
        if (token == null || token.isEmpty()) {
            Log.e("CheckoutActivity", "Access token is null or empty");
            runOnUiThread(() -> Toast.makeText(this, "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show());
            finish();
            return;
        }

        Log.d("CheckoutActivity", "Sending request to /order/detail/" + orderId + " with token: " + token);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<OrderDetailResponse> call = apiService.getOrderDetails("Bearer " + token, orderId);

        // Hiển thị ProgressDialog khi bắt đầu tải dữ liệu
        progressDialog.setMessage("Đang tải chi tiết đơn hàng...");
        progressDialog.show();

        call.enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                progressDialog.dismiss();
                Log.d("CheckoutActivity", "Received response from /order/detail/" + orderId + ": " + response.toString());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    OrderDetailResponse.Order order = response.body().getOrder();
                    Log.d("CheckoutActivity", "Order details: " + order.toString());
                    reorderItems = order.getItems();
                    totalPrice = order.getTotalPrice();

                    // Log dữ liệu trước khi lọc
                    Log.d("CheckoutActivity", "Before filtering - reorderItems: " + (reorderItems != null ? reorderItems.toString() : "null"));

                    // Kiểm tra nếu reorderItems là null hoặc rỗng
                    if (reorderItems == null || reorderItems.isEmpty()) {
                        Log.e("CheckoutActivity", "reorderItems is null or empty");
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Không có sản phẩm trong đơn hàng", Toast.LENGTH_SHORT).show());
                        finish();
                        return;
                    }

                    // Lọc bỏ các mục không hợp lệ
                    reorderItems.removeIf(item -> {
                        String productId = item.getProductId();
                        int quantity = item.getQuantity();
                        boolean isInvalid = productId == null || productId.trim().isEmpty() || productId.equals("0") || quantity <= 0;
                        if (isInvalid) {
                            Log.d("CheckoutActivity", "Filtered out invalid item: productId=" + productId + ", quantity=" + quantity);
                        }
                        return isInvalid;
                    });

                    // Log dữ liệu sau khi lọc
                    Log.d("CheckoutActivity", "After filtering - reorderItems: " + reorderItems.toString());

                    if (reorderItems.isEmpty()) {
                        Log.d("CheckoutActivity", "No valid items to reorder");
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Không có sản phẩm hợp lệ để đặt hàng", Toast.LENGTH_SHORT).show());
                        finish();
                        return;
                    }

                    // Cập nhật totalPrice dựa trên các mục hợp lệ
                    double updatedTotalPrice = 0;
                    for (OrderDetailResponse.OrderItem item : reorderItems) {
                        updatedTotalPrice += item.getPrice() * item.getQuantity(); // Tính giá dựa trên số lượng
                    }
                    totalPrice = updatedTotalPrice;

                    isItemsLoaded = true; // Đánh dấu dữ liệu đã được tải

                    // Tạo bản sao final để sử dụng trong lambda
                    final double finalTotalPrice = totalPrice;
                    runOnUiThread(() -> {
                        btnConfirmOrder.setEnabled(true); // Kích hoạt nút "Xác nhận"
                        txtTotalCost.setText(String.format("Total Cost: %.0f VND", finalTotalPrice));
                        displayOrderItems();
                    });
                } else {
                    String errorMessage = "Không thể tải chi tiết đơn hàng";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    final String finalErrorMessage = errorMessage;
                    Log.e("CheckoutActivity", finalErrorMessage);
                    runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, finalErrorMessage, Toast.LENGTH_SHORT).show());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                progressDialog.dismiss();
                final String errorMessage = "Lỗi: " + t.getMessage();
                Log.e("CheckoutActivity", "Failed to load order details: " + t.getMessage());
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                finish();
            }
        });
    }

    private void displayOrderItems() {
        if (reorderItems == null) return;

        orderItemsContainer.removeAllViews();
        for (OrderDetailResponse.OrderItem item : reorderItems) {
            TextView itemView = new TextView(this);
            itemView.setText(String.format("Product: %s - Quantity: %d - Price: %.0f VND",
                    item.getProductName(), item.getQuantity(), item.getPrice() * item.getQuantity()));
            itemView.setTextSize(16);
            itemView.setTextColor(getResources().getColor(android.R.color.black));
            itemView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemView.setPadding(0, 0, 0, 8);

            orderItemsContainer.addView(itemView);
        }

        txtTotalCost.setText(String.format("Total Cost: %.0f VND", totalPrice));
    }

    private void confirmOrder() {
        if (!isItemsLoaded) {
            Log.e("CheckoutActivity", "Items not loaded yet");
            runOnUiThread(() -> Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại sau", Toast.LENGTH_SHORT).show());
            return;
        }

        String fullName = edtFullName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        final String paymentMethod = rbCOD.isChecked() ? "COD" : "ZaloPay";

        if (fullName.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show());
            return;
        }

        if (reorderItems == null || reorderItems.isEmpty()) {
            Log.e("CheckoutActivity", "No items to order");
            runOnUiThread(() -> Toast.makeText(this, "Không có sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show());
            return;
        }

        // Thêm kiểm tra bổ sung để đảm bảo không có mục không hợp lệ
        boolean hasInvalidItems = reorderItems.stream().anyMatch(item ->
                item.getProductId() == null ||
                        item.getProductId().trim().isEmpty() ||
                        item.getProductId().equals("0") ||
                        item.getQuantity() <= 0
        );

        if (hasInvalidItems) {
            Log.e("CheckoutActivity", "Invalid items found in reorderItems: " + reorderItems.toString());
            runOnUiThread(() -> Toast.makeText(this, "Danh sách sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show());
            return;
        }

        List<OrderRequest.OrderItem> items = new ArrayList<>();
        for (OrderDetailResponse.OrderItem item : reorderItems) {
            items.add(new OrderRequest.OrderItem(item.getProductId(), item.getQuantity()));
        }

        String token = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");
        if (token == null || token.isEmpty()) {
            Log.e("CheckoutActivity", "Access token is null or empty");
            runOnUiThread(() -> Toast.makeText(this, "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show());
            return;
        }

        final OrderRequest orderRequest = new OrderRequest(items, paymentMethod, fullName, phoneNumber, address, totalPrice);

        Log.d("CheckoutActivity", "Creating new order with request: " + orderRequest.toString());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        progressDialog.show();
        Call<OrderResponse> call = apiService.createOrder("Bearer " + token, orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    final int newOrderId = orderResponse.getOrderId();
                    if (paymentMethod.equals("COD")) {
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công! Order ID: " + newOrderId, Toast.LENGTH_LONG).show());
                        finish();
                    } else if (paymentMethod.equals("ZaloPay")) {
                        final String zaloPayUrl = orderResponse.getZaloPayUrl();
                        if (zaloPayUrl != null && !zaloPayUrl.isEmpty()) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                intent.setPackage("com.zing.zalo.sandbox");
                                Log.i("ZaloPay", "Opening ZaloPay Sandbox with URL: " + zaloPayUrl);
                                startActivityForResult(intent, 1);
                            } catch (ActivityNotFoundException e) {
                                final String errorMessage = "ZaloPay Sandbox not found: " + e.getMessage();
                                Log.e("ZaloPay", errorMessage);
                                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "ZaloPay Sandbox không được cài đặt", Toast.LENGTH_SHORT).show());
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                startActivityForResult(browserIntent, 1);
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Lỗi: Không nhận được URL ZaloPay", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    String errorMessage = "Lỗi: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = "Lỗi: " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    final String finalErrorMessage = errorMessage;
                    Log.e("CheckoutActivity", finalErrorMessage);
                    runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                final String errorMessage = "Lỗi kết nối: " + t.getMessage();
                Log.e("CheckoutActivity", "Failed to create order: " + t.getMessage());
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            checkOrderStatus(orderId);
        }
    }

    private void checkOrderStatus(final String orderId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        progressDialog.show();
        Call<OrderStatusResponse> call = apiService.checkOrderStatus(Integer.parseInt(orderId));
        call.enqueue(new Callback<OrderStatusResponse>() {
            @Override
            public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    final String status = response.body().getPaymentStatus();
                    if ("paid".equals(status)) {
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Thanh toán ZaloPay thành công!", Toast.LENGTH_LONG).show());
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Thanh toán ZaloPay chưa hoàn tất", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Lỗi: Đơn hàng không tồn tại hoặc server lỗi", Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                progressDialog.dismiss();
                final String errorMessage = "Lỗi mạng: " + t.getMessage();
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }
}