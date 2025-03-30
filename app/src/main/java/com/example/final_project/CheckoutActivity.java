package com.example.final_project;

import static android.widget.Toast.makeText;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderDetailResponse;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.AddEditAddressActivity;
import com.example.final_project.Log.LogInActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhoneNumber, edtStreetAddress;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbZaloPay;
    private Button btnConfirmOrder, btnCancelOrder, btnAddNewAddress;
    private LinearLayout orderItemsContainer, newAddressContainer;
    private TextView txtTotalCost;
    private Spinner spinnerSavedAddress, spinnerProvince, spinnerDistrict, spinnerWard;
    private ProgressDialog progressDialog;
    private String orderId;
    private boolean isReorder;
    private List<OrderDetailResponse.OrderItem> reorderItems;
    private double totalPrice;
    private boolean isItemsLoaded = false;
    private int lastPendingOrderId = -1;
    private List<Address> savedAddressList = new ArrayList<>();
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    private ApiService apiService;

    private static final String KEY_PENDING_ORDER_ID = "lastPendingOrderId";
    private static final int REQUEST_ADD_ADDRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        // Khôi phục trạng thái
        if (savedInstanceState != null) {
            lastPendingOrderId = savedInstanceState.getInt(KEY_PENDING_ORDER_ID, -1);
        }

        // Khởi tạo ApiService
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Ánh xạ các thành phần từ layout
        edtFullName = findViewById(R.id.edt_full_name);
        edtPhoneNumber = findViewById(R.id.edt_phone_number);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        rbCOD = findViewById(R.id.rb_cod);
        rbZaloPay = findViewById(R.id.rb_zalo_pay);
        btnConfirmOrder = findViewById(R.id.btn_confirm_order);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnAddNewAddress = findViewById(R.id.btn_add_new_address);
        orderItemsContainer = findViewById(R.id.order_items_container);
        txtTotalCost = findViewById(R.id.txt_total_cost);
        spinnerSavedAddress = findViewById(R.id.spinner_saved_address);
        newAddressContainer = findViewById(R.id.new_address_container);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerWard = findViewById(R.id.spinner_ward);
        edtStreetAddress = findViewById(R.id.edt_street_address);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        btnConfirmOrder.setEnabled(false);

        orderId = getIntent().getStringExtra("orderId");
        isReorder = getIntent().getBooleanExtra("isReorder", false);

        if (isReorder) {
            loadReorderItems();
        } else {
            makeText(this, "Chức năng này chỉ hỗ trợ mua lại đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupSpinners();

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
        btnCancelOrder.setOnClickListener(v -> finish());
        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, AddEditAddressActivity.class);
            startActivityForResult(intent, REQUEST_ADD_ADDRESS);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastPendingOrderId != -1) {
            checkOrderStatus(String.valueOf(lastPendingOrderId));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PENDING_ORDER_ID, lastPendingOrderId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ADDRESS && resultCode == RESULT_OK) {
            loadSavedAddresses(apiService, getUserToken());
        } else if (requestCode == 1 && lastPendingOrderId != -1) {
            checkOrderStatus(String.valueOf(lastPendingOrderId));
        }
    }

    private void setupSpinners() {
        String token = getUserToken();
        loadSavedAddresses(apiService, token);

        // Xử lý Spinner địa chỉ đã lưu
        spinnerSavedAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // "Chọn địa chỉ đã lưu"
                    newAddressContainer.setVisibility(View.GONE);
                } else {
                    newAddressContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newAddressContainer.setVisibility(View.GONE);
            }
        });

        // Xử lý Spinner tỉnh
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && !provinceList.isEmpty()) {
                    Province selectedProvince = provinceList.get(position);
                    loadDistricts(apiService, selectedProvince.getCode());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Xử lý Spinner huyện
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && !districtList.isEmpty()) {
                    District selectedDistrict = districtList.get(position);
                    loadWards(apiService, selectedDistrict.getCode());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSavedAddresses(ApiService apiService, String token) {
        String userIdStr = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", null);
        if (userIdStr == null) {
            makeText(this, "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }
        int userId = Integer.parseInt(userIdStr);
        Call<List<Address>> call = apiService.getUserAddresses(userId);
        call.enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedAddressList = response.body();
                    List<String> addressOptions = new ArrayList<>();
                    addressOptions.add("Chọn địa chỉ đã lưu");
                    for (Address address : savedAddressList) {
                        addressOptions.add(address.toString());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_spinner_item, addressOptions);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSavedAddress.setAdapter(adapter);
                } else {
                    makeText(CheckoutActivity.this, "Không thể tải địa chỉ đã lưu: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("CheckoutActivity", "Response code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                makeText(CheckoutActivity.this, "Lỗi tải địa chỉ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CheckoutActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void loadProvinces() {
        Call<List<Province>> call = apiService.getProvinces();
        call.enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList = response.body();
                    List<String> provinceNames = new ArrayList<>();
                    for (Province province : provinceList) {
                        provinceNames.add(province.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_spinner_item, provinceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProvince.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                makeText(CheckoutActivity.this, "Lỗi tải tỉnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(ApiService apiService, int provinceCode) {
        Call<List<District>> call = apiService.getDistricts(provinceCode);
        call.enqueue(new Callback<List<District>>() {
            @Override
            public void onResponse(Call<List<District>> call, Response<List<District>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districtList = response.body();
                    List<String> districtNames = new ArrayList<>();
                    for (District district : districtList) {
                        districtNames.add(district.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_spinner_item, districtNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {
                makeText(CheckoutActivity.this, "Lỗi tải huyện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(ApiService apiService, int districtCode) {
        Call<List<Ward>> call = apiService.getWards(districtCode);
        call.enqueue(new Callback<List<Ward>>() {
            @Override
            public void onResponse(Call<List<Ward>> call, Response<List<Ward>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wardList = response.body();
                    List<String> wardNames = new ArrayList<>();
                    for (Ward ward : wardList) {
                        wardNames.add(ward.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_spinner_item, wardNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWard.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Ward>> call, Throwable t) {
                makeText(CheckoutActivity.this, "Lỗi tải xã: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReorderItems() {
        String token = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");
        if (token.isEmpty()) {
            Log.e("CheckoutActivity", "Access token is null or empty");
            makeText(this, "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<OrderDetailResponse> call = apiService.getOrderDetails("Bearer " + token, orderId);

        progressDialog.setMessage("Đang tải chi tiết đơn hàng...");
        progressDialog.show();

        call.enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    OrderDetailResponse.Order order = response.body().getOrder();
                    reorderItems = order.getItems();
                    totalPrice = order.getTotalPrice();

                    if (reorderItems == null || reorderItems.isEmpty()) {
                        Log.e("CheckoutActivity", "reorderItems is null or empty");
                        makeText(CheckoutActivity.this, "Không có sản phẩm trong đơn hàng", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    reorderItems.removeIf(item -> item.getProductId() == null || item.getProductId().trim().isEmpty() || item.getQuantity() <= 0);

                    if (reorderItems.isEmpty()) {
                        Log.d("CheckoutActivity", "No valid items to reorder");
                        makeText(CheckoutActivity.this, "Không có sản phẩm hợp lệ để đặt hàng", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    double updatedTotalPrice = 0;
                    for (OrderDetailResponse.OrderItem item : reorderItems) {
                        updatedTotalPrice += item.getPrice() * item.getQuantity();
                    }
                    totalPrice = updatedTotalPrice;

                    isItemsLoaded = true;

                    btnConfirmOrder.setEnabled(true);
                    displayOrderItems();
                } else {
                    String errorMessage = "Không thể tải chi tiết đơn hàng: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("CheckoutActivity", errorMessage);
                    makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("CheckoutActivity", "Failed to load order details: " + t.getMessage());
                makeText(CheckoutActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrderItems() {
        if (reorderItems == null) return;

        orderItemsContainer.removeAllViews();
        for (OrderDetailResponse.OrderItem item : reorderItems) {
            TextView itemView = new TextView(this);
            itemView.setText(String.format("%s - Quantity: %d - Price: %.0f VND",
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
            makeText(this, "Đang tải dữ liệu, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String paymentMethod = rbCOD.isChecked() ? "COD" : "ZaloPay";
        String address;

        int selectedPosition = spinnerSavedAddress.getSelectedItemPosition();
        if (selectedPosition == 0) { // Chưa chọn địa chỉ
            makeText(this, "Vui lòng chọn một địa chỉ đã lưu hoặc thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
            return;
        } else { // Chọn địa chỉ đã lưu
            address = savedAddressList.get(selectedPosition - 1).toString();
        }

        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
            makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reorderItems == null || reorderItems.isEmpty()) {
            Log.e("CheckoutActivity", "No items to order");
            makeText(this, "Không có sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItem> items = new ArrayList<>();
        for (OrderDetailResponse.OrderItem item : reorderItems) {
            items.add(new OrderRequest.OrderItem(item.getProductId(), item.getQuantity()));
        }

        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            makeText(this, "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderRequest orderRequest = new OrderRequest(items, paymentMethod, fullName, phoneNumber, address, totalPrice);

        progressDialog.show();
        Call<OrderResponse> call = apiService.createOrder("Bearer " + token, orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    lastPendingOrderId = orderResponse.getPendingOrderId();
                    if (paymentMethod.equals("COD")) {
                        makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    } else if (paymentMethod.equals("ZaloPay")) {
                        String zaloPayUrl = orderResponse.getZaloPayUrl();
                        if (zaloPayUrl != null && !zaloPayUrl.isEmpty()) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                intent.setPackage("com.zing.zalo.sandbox");
                                startActivityForResult(intent, 1);
                            } catch (ActivityNotFoundException e) {
                                makeText(CheckoutActivity.this, "ZaloPay Sandbox không được cài đặt, mở trình duyệt...", Toast.LENGTH_SHORT).show();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                startActivityForResult(browserIntent, 1);
                            }
                        } else {
                            makeText(CheckoutActivity.this, "Lỗi: Không nhận được URL ZaloPay", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    String errorMessage = "Lỗi: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("CheckoutActivity", errorMessage);
                    makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("CheckoutActivity", "Failed to create order: " + t.getMessage());
                makeText(CheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    private void checkOrderStatus(String pendingOrderId) {
        progressDialog.show();
        Call<OrderStatusResponse> call = apiService.checkOrderStatus(Integer.parseInt(pendingOrderId));
        call.enqueue(new Callback<OrderStatusResponse>() {
            @Override
            public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getPaymentStatus();
                    if ("paid".equals(status)) {
                        makeText(CheckoutActivity.this, "Thanh toán ZaloPay thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        makeText(CheckoutActivity.this, "Thanh toán ZaloPay chưa hoàn tất", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    makeText(CheckoutActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CheckoutActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}