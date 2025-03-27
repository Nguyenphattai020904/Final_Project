package com.example.final_project.Fragments;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.Products.Product;
import com.example.final_project.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {
    private EditText edtFullName, edtAddress, edtPhoneNumber;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbZaloPay;
    private Button btnConfirmOrder, btnCancelOrder;
    private LinearLayout orderItemsContainer;
    private TextView txtTotalCost;
    private int lastOrderId = -1;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment, container, false);

        edtFullName = view.findViewById(R.id.edt_full_name);
        edtAddress = view.findViewById(R.id.edt_address);
        edtPhoneNumber = view.findViewById(R.id.edt_phone_number);
        rgPaymentMethod = view.findViewById(R.id.rg_payment_method);
        rbCOD = view.findViewById(R.id.rb_cod);
        rbZaloPay = view.findViewById(R.id.rb_zalo_pay);
        btnConfirmOrder = view.findViewById(R.id.btn_confirm_order);
        btnCancelOrder = view.findViewById(R.id.btn_cancel_order);
        orderItemsContainer = view.findViewById(R.id.order_items_container);
        txtTotalCost = view.findViewById(R.id.txt_total_cost);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang xử lý đơn hàng...");
        progressDialog.setCancelable(false);

        displayOrderItems();

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
        btnCancelOrder.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void displayOrderItems() {
        CartFragment cartFragment = (CartFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CartFragment");
        List<Product> selectedItems = cartFragment != null ? cartFragment.getSelectedItems() : new ArrayList<>();
        double totalCost = 0;

        for (Product product : selectedItems) {
            TextView itemView = new TextView(getContext());
            itemView.setText(String.format("%s - Quantity: %d - Price: %.0f VND",
                    product.getName(), product.getQuantity(), product.getPrice() * product.getQuantity()));
            itemView.setTextSize(16);
            itemView.setTextColor(getResources().getColor(android.R.color.black));
            itemView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemView.setPadding(0, 0, 0, 8);

            orderItemsContainer.addView(itemView);
            totalCost += product.getPrice() * product.getQuantity();
        }

        txtTotalCost.setText(String.format("Total Cost: %.0f VND", totalCost));
    }

    private void confirmOrder() {
        String fullName = edtFullName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String paymentMethod = rbCOD.isChecked() ? "COD" : "ZaloPay";

        if (fullName.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        CartFragment cartFragment = (CartFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CartFragment");
        List<Product> selectedItems = cartFragment != null ? cartFragment.getSelectedItems() : new ArrayList<>();

        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItem> items = new ArrayList<>();
        double totalCost = 0;
        for (Product product : selectedItems) {
            items.add(new OrderRequest.OrderItem(product.getProductId(), product.getQuantity()));
            totalCost += product.getPrice() * product.getQuantity();
        }

        // Lấy token từ SharedPreferences
        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderRequest orderRequest = new OrderRequest(items, paymentMethod, fullName, phoneNumber, address, totalCost);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        progressDialog.show();
        Call<OrderResponse> call = apiService.createOrder("Bearer " + token, orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    lastOrderId = orderResponse.getOrderId();
                    if (paymentMethod.equals("COD")) {
                        removeSelectedItems(selectedItems);
                        cartFragment.updateCartFromManager(); // Cập nhật giao diện giỏ hàng
                        Toast.makeText(getContext(), "Đặt hàng thành công! Order ID: " + lastOrderId, Toast.LENGTH_LONG).show();
                        navigateBackToHome();
                    } else if (paymentMethod.equals("ZaloPay")) {
                        String zaloPayUrl = orderResponse.getZaloPayUrl();
                        if (zaloPayUrl != null && !zaloPayUrl.isEmpty()) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                intent.setPackage("com.zing.zalo.sandbox");
                                Log.i("ZaloPay", "Opening ZaloPay Sandbox with URL: " + zaloPayUrl);
                                startActivityForResult(intent, 1);
                            } catch (ActivityNotFoundException e) {
                                Log.e("ZaloPay", "ZaloPay Sandbox not found: " + e.getMessage());
                                Toast.makeText(getContext(), "ZaloPay Sandbox không được cài đặt", Toast.LENGTH_SHORT).show();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                startActivityForResult(browserIntent, 1);
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi: Không nhận được URL ZaloPay", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Xử lý lỗi chi tiết hơn
                    String errorMessage = "Lỗi: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = "Lỗi: " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);
        Log.d("PaymentFragment", "Retrieved token: " + token);
        return token;
    }

    private void removeSelectedItems(List<Product> selectedItems) {
        CartManager.getInstance().getCartItems().removeAll(selectedItems);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && lastOrderId != -1) {
            checkOrderStatus(lastOrderId);
        }
    }

    private void navigateBackToHome() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void checkOrderStatus(int orderId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        progressDialog.show();
        Call<OrderStatusResponse> call = apiService.checkOrderStatus(orderId);
        call.enqueue(new Callback<OrderStatusResponse>() {
            @Override
            public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                progressDialog.dismiss();
                Log.i("OrderStatus", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getPaymentStatus();
                    Log.i("OrderStatus", "Payment status: " + status);
                    if ("paid".equals(status)) {
                        CartFragment cartFragment = (CartFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CartFragment");
                        if (cartFragment != null) {
                            removeSelectedItems(cartFragment.getSelectedItems());
                            cartFragment.updateCartFromManager(); // Cập nhật giao diện giỏ hàng
                        }
                        Toast.makeText(getContext(), "Thanh toán ZaloPay thành công!", Toast.LENGTH_LONG).show();
                        navigateBackToHome();
                    } else {
                        Toast.makeText(getContext(), "Thanh toán ZaloPay chưa hoàn tất", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi: Đơn hàng không tồn tại hoặc server lỗi (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("OrderStatus", "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}