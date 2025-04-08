package com.example.final_project.Activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.final_project.API_Reponse.VoucherResponse;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.API_Requests.VoucherRequest;
import com.example.final_project.Adapter.VoucherSpinnerAdapter;
import com.example.final_project.Address.Address;
import com.example.final_project.Address.District;
import com.example.final_project.Log.LogInActivity;
import com.example.final_project.Address.Province;
import com.example.final_project.R;
import com.example.final_project.Voucher;
import com.example.final_project.Address.Ward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhoneNumber;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbZaloPay;
    private Button btnConfirmOrder, btnCancelOrder, btnAddNewAddress;
    private LinearLayout orderItemsContainer, newAddressContainer;
    private TextView txtTotalCost, txtDiscount, txtFinalCost;
    private Spinner spinnerSavedAddress, spinnerProvince, spinnerDistrict, spinnerWard, spinnerVouchers;
    private ProgressDialog progressDialog;
    private String orderId;
    private boolean isReorder;
    private List<OrderDetailResponse.OrderItem> reorderItems;
    private double totalPrice, discount = 0, finalPrice;
    private boolean isItemsLoaded = false;
    private int lastPendingOrderId = -1;
    private boolean isZaloPayPaymentPending = false;
    private List<Address> savedAddressList = new ArrayList<>();
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    private List<Voucher> voucherList = new ArrayList<>();
    private ApiService apiService;
    private int maxRetries = 5;
    private int retryCount = 0;

    private static final String KEY_PENDING_ORDER_ID = "lastPendingOrderId";
    private static final String KEY_PAYMENT_PENDING = "isZaloPayPaymentPending";
    private static final int REQUEST_ADD_ADDRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        if (savedInstanceState != null) {
            lastPendingOrderId = savedInstanceState.getInt(KEY_PENDING_ORDER_ID, -1);
            isZaloPayPaymentPending = savedInstanceState.getBoolean(KEY_PAYMENT_PENDING, false);
            Log.d("CheckoutActivity", "Restored state: lastPendingOrderId=" + lastPendingOrderId + ", isZaloPayPaymentPending=" + isZaloPayPaymentPending);
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

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
        txtDiscount = findViewById(R.id.txt_discount);
        txtFinalCost = findViewById(R.id.txt_final_cost);
        spinnerSavedAddress = findViewById(R.id.spinner_saved_address);
        newAddressContainer = findViewById(R.id.new_address_container);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerWard = findViewById(R.id.spinner_ward);
        spinnerVouchers = findViewById(R.id.spinner_vouchers);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        btnConfirmOrder.setEnabled(false);

        orderId = getIntent().getStringExtra("orderId");
        isReorder = getIntent().getBooleanExtra("isReorder", false);

        if (isReorder) {
            loadReorderItems();
        } else {
            Toast.makeText(this, "Chức năng này chỉ hỗ trợ mua lại đơn hàng", Toast.LENGTH_SHORT).show();
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

        handleDeepLink(getIntent());
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            if ("finalproject".equals(data.getScheme()) && "payment".equals(data.getHost())) {
                String orderIdStr = data.getQueryParameter("orderId");
                if (orderIdStr != null) {
                    lastPendingOrderId = Integer.parseInt(orderIdStr);
                    isZaloPayPaymentPending = true;
                    retryCount = 0;
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                    checkOrderStatus(lastPendingOrderId);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isZaloPayPaymentPending && lastPendingOrderId != -1) {
            retryCount = 0;
            progressDialog.show();
            checkOrderStatus(lastPendingOrderId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PENDING_ORDER_ID, lastPendingOrderId);
        outState.putBoolean(KEY_PAYMENT_PENDING, isZaloPayPaymentPending);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ADDRESS && resultCode == RESULT_OK) {
            loadSavedAddresses(apiService, getUserToken());
        } else if (requestCode == 1 && lastPendingOrderId != -1) {
            isZaloPayPaymentPending = true;
            retryCount = 0;
            progressDialog.show();
            checkOrderStatus(lastPendingOrderId);
        }
    }

    private void setupSpinners() {
        String token = getUserToken();
        loadSavedAddresses(apiService, token);
        setupVoucherSpinner();

        spinnerSavedAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    newAddressContainer.setVisibility(View.VISIBLE);
                    loadProvinces(apiService);
                } else {
                    newAddressContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newAddressContainer.setVisibility(View.VISIBLE);
            }
        });

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

    private void setupVoucherSpinner() {
        String userIdStr = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", "0");
        int userId = Integer.parseInt(userIdStr);

        Call<List<Voucher>> call = apiService.getVouchers(userId);
        call.enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    voucherList = response.body();
                    voucherList.add(0, new Voucher(0, 0, "Chọn voucher", 0, "none", 0, null, 0, null));
                    VoucherSpinnerAdapter adapter = new VoucherSpinnerAdapter(CheckoutActivity.this, voucherList, totalPrice);
                    spinnerVouchers.setAdapter(adapter);

                    spinnerVouchers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Voucher selectedVoucher = voucherList.get(position);
                            if (position == 0) {
                                discount = 0;
                            } else if (selectedVoucher.isApplicable(totalPrice)) {
                                applyVoucher(selectedVoucher);
                                addNotification("Áp dụng voucher '" + selectedVoucher.getVoucherName() + "' thành công! Giảm: " + discount + " VND");
                            } else {
                                Toast.makeText(CheckoutActivity.this, "Đơn hàng không đủ điều kiện cho voucher này", Toast.LENGTH_SHORT).show();
                                spinnerVouchers.setSelection(0);
                                discount = 0;
                            }
                            updateCostDisplay();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi tải voucher: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyVoucher(Voucher voucher) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        VoucherRequest request = new VoucherRequest(voucher.getVoucherId(), totalPrice);
        Call<VoucherResponse> call = apiService.applyVoucher(request);
        call.enqueue(new Callback<VoucherResponse>() {
            @Override
            public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    discount = response.body().getDiscount();
                    finalPrice = totalPrice - discount;
                    updateCostDisplay();
                } else {
                    discount = 0;
                    finalPrice = totalPrice;
                    updateCostDisplay();
                }
            }

            @Override
            public void onFailure(Call<VoucherResponse> call, Throwable t) {
                discount = 0;
                finalPrice = totalPrice;
                updateCostDisplay();
            }
        });
    }

    private void updateCostDisplay() {
        txtTotalCost.setText(String.format("Tổng tiền: %.0f VND", totalPrice));
        txtDiscount.setText(String.format("Giảm giá: %.0f VND", discount));
        txtFinalCost.setText(String.format("Thành tiền: %.0f VND", totalPrice - discount));
    }

    private void loadSavedAddresses(ApiService apiService, String token) {
        String userIdStr = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", null);
        if (userIdStr == null) {
            Toast.makeText(this, "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CheckoutActivity.this, "Không thể tải địa chỉ đã lưu: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("CheckoutActivity", "Response code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi tải địa chỉ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CheckoutActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void loadProvinces(ApiService apiService) {
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
                Toast.makeText(CheckoutActivity.this, "Lỗi tải tỉnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CheckoutActivity.this, "Lỗi tải huyện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CheckoutActivity.this, "Lỗi tải xã: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReorderItems() {
        String token = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");
        if (token.isEmpty()) {
            Log.e("CheckoutActivity", "Access token is null or empty");
            Toast.makeText(this, "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
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
                    totalPrice = 0;
                    finalPrice = totalPrice;

                    if (reorderItems == null || reorderItems.isEmpty()) {
                        Log.e("CheckoutActivity", "reorderItems is null or empty");
                        Toast.makeText(CheckoutActivity.this, "Không có sản phẩm trong đơn hàng", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    reorderItems.removeIf(item -> item.getProductId() == null || item.getProductId().trim().isEmpty() || item.getQuantity() <= 0);

                    if (reorderItems.isEmpty()) {
                        Log.d("CheckoutActivity", "No valid items to reorder");
                        Toast.makeText(CheckoutActivity.this, "Không có sản phẩm hợp lệ để đặt hàng", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    for (OrderDetailResponse.OrderItem item : reorderItems) {
                        totalPrice += item.getPrice() * item.getQuantity();
                        Log.d("CheckoutActivity", "Product: " + item.getProductName() + ", Unit Price: " + item.getPrice() + ", Qty: " + item.getQuantity());
                    }
                    finalPrice = totalPrice;

                    isItemsLoaded = true;

                    btnConfirmOrder.setEnabled(true);
                    displayOrderItems();
                    updateCostDisplay();
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
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("CheckoutActivity", "Failed to load order details: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrderItems() {
        if (reorderItems == null) return;

        orderItemsContainer.removeAllViews();
        for (OrderDetailResponse.OrderItem item : reorderItems) {
            TextView itemView = new TextView(this);
            itemView.setText(String.format("%s - Quantity: %d - Unit Price: %.0f VND",
                    item.getProductName(), item.getQuantity(), item.getPrice()));
            itemView.setTextSize(16);
            itemView.setTextColor(getResources().getColor(android.R.color.black));
            itemView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemView.setPadding(0, 0, 0, 8);
            orderItemsContainer.addView(itemView);
        }
    }

    private void confirmOrder() {
        if (!isItemsLoaded) {
            Log.e("CheckoutActivity", "Items not loaded yet");
            Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String paymentMethod = rbCOD.isChecked() ? "COD" : "ZaloPay";
        String address;

        int selectedAddressPosition = spinnerSavedAddress.getSelectedItemPosition();
        if (selectedAddressPosition == 0) {
            if (spinnerProvince.getSelectedItem() == null || spinnerDistrict.getSelectedItem() == null || spinnerWard.getSelectedItem() == null) {
                Toast.makeText(this, "Vui lòng chọn đầy đủ tỉnh, huyện, xã", Toast.LENGTH_SHORT).show();
                return;
            }
            address = spinnerWard.getSelectedItem() + ", " + spinnerDistrict.getSelectedItem() + ", " + spinnerProvince.getSelectedItem();
        } else {
            address = savedAddressList.get(selectedAddressPosition - 1).toString();
        }

        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reorderItems == null || reorderItems.isEmpty()) {
            Log.e("CheckoutActivity", "No items to order");
            Toast.makeText(this, "Không có sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItem> items = new ArrayList<>();
        for (OrderDetailResponse.OrderItem item : reorderItems) {
            items.add(new OrderRequest.OrderItem(item.getProductId(), item.getQuantity()));
        }

        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        int voucherId = spinnerVouchers.getSelectedItemPosition() > 0 ?
                voucherList.get(spinnerVouchers.getSelectedItemPosition()).getVoucherId() : 0;

        calculateTotalPrice(token, items, voucherId, paymentMethod, fullName, phoneNumber, address);
    }

    private void calculateTotalPrice(String token, List<OrderRequest.OrderItem> items, int voucherId, String paymentMethod, String fullName, String phoneNumber, String address) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("items", items);
        requestBody.put("voucher_id", voucherId);

        progressDialog.show();
        Call<Map<String, Double>> call = apiService.calculateTotal("Bearer " + token, requestBody);
        call.enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double serverTotalPrice = response.body().get("total_price");
                    Log.d("CheckoutActivity", "Server calculated total_price: " + serverTotalPrice);
                    createOrder(token, items, paymentMethod, fullName, phoneNumber, address, serverTotalPrice, voucherId);
                } else {
                    progressDialog.dismiss();
                    String errorMessage = "Lỗi tính tổng giá: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("CheckoutActivity", "Calculate total error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối khi tính tổng giá: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CheckoutActivity", "Calculate total network error: " + t.getMessage());
            }
        });
    }

    private void createOrder(String token, List<OrderRequest.OrderItem> items, String paymentMethod, String fullName, String phoneNumber, String address, double totalPrice, int voucherId) {
        OrderRequest orderRequest = new OrderRequest(items, paymentMethod, fullName, phoneNumber, address, totalPrice, voucherId);

        Log.d("CheckoutActivity", "Sending total_price: " + totalPrice);

        Call<OrderResponse> call = apiService.createOrder("Bearer " + token, orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    lastPendingOrderId = orderResponse.getPendingOrderId();
                    Log.d("CheckoutActivity", "Order Response: " + orderResponse.toString());

                    if (paymentMethod.equals("COD")) {
                        Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        addNotification("Đặt hàng thành công qua COD! Đơn hàng #" + lastPendingOrderId + " đang được xử lý.");
                        navigateBackToHome();
                    } else if (paymentMethod.equals("ZaloPay")) {
                        String zaloPayUrl = orderResponse.getZaloPayUrl();
                        if (zaloPayUrl != null && !zaloPayUrl.isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                            try {
                                startActivityForResult(intent, 1);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(CheckoutActivity.this, "ZaloPay Sandbox không được cài đặt. Mở trình duyệt.", Toast.LENGTH_LONG).show();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                startActivityForResult(browserIntent, 1);
                            }
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Lỗi: Không nhận được URL ZaloPay từ server", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    String errorMessage = "Lỗi: " + response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("CheckoutActivity", errorMessage);
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("CheckoutActivity", "Failed to create order: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    public void checkOrderStatus(int pendingOrderId) {
        if (retryCount >= maxRetries) {
            progressDialog.dismiss();
            Toast.makeText(this, "Hết thời gian chờ thanh toán ZaloPay. Vui lòng kiểm tra trạng thái trong lịch sử.", Toast.LENGTH_LONG).show();
            isZaloPayPaymentPending = false;
            retryCount = 0;
            navigateBackToHome();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("Đang xử lý thanh toán ZaloPay...");
            progressDialog.show();
        }

        Call<OrderStatusResponse> call = apiService.checkOrderStatus(pendingOrderId);
        call.enqueue(new Callback<OrderStatusResponse>() {
            @Override
            public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getPaymentStatus();
                    Integer newOrderId = response.body().getNewOrderId();
                    Log.d("CheckoutActivity", "Order status for " + pendingOrderId + ": " + status + ", newOrderId: " + newOrderId);

                    if ("paid".equals(status)) {
                        progressDialog.dismiss();
                        if (newOrderId != null && newOrderId != pendingOrderId) {
                            lastPendingOrderId = newOrderId; // Cập nhật lastPendingOrderId
                            Log.d("CheckoutActivity", "Updated lastPendingOrderId to: " + lastPendingOrderId);
                        }
                        Toast.makeText(CheckoutActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                        addNotification("Thanh toán thành công qua ZaloPay! Đơn hàng #" + lastPendingOrderId);
                        isZaloPayPaymentPending = false;
                        retryCount = 0;
                        navigateBackToHome();
                    } else if ("waiting_payment".equals(status)) {
                        retryCount++;
                        Log.d("CheckoutActivity", "Retry " + retryCount + "/" + maxRetries + " for order " + pendingOrderId);
                        new Handler().postDelayed(() -> checkOrderStatus(pendingOrderId), 2000);
                    } else {
                        progressDialog.dismiss();
                        Log.w("CheckoutActivity", "Unexpected status received: " + status);
                        Toast.makeText(CheckoutActivity.this, "Trạng thái thanh toán không xác định: " + status, Toast.LENGTH_LONG).show();
                        isZaloPayPaymentPending = false;
                        retryCount = 0;
                        navigateBackToHome();
                    }
                } else {
                    progressDialog.dismiss();
                    String errorMessage = response.code() == 404 ? "Đơn hàng không tồn tại hoặc đã hết hạn" : "Lỗi server: " + response.message();
                    Log.e("CheckoutActivity", "Response failed with code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    isZaloPayPaymentPending = false;
                    retryCount = 0;
                    navigateBackToHome();
                }
            }

            @Override
            public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                retryCount++;
                Log.e("CheckoutActivity", "Network error on retry " + retryCount + "/" + maxRetries + ": " + t.getMessage());
                new Handler().postDelayed(() -> checkOrderStatus(pendingOrderId), 2000);
            }
        });
    }

    private void addNotification(String message) {
        updateHomeBadge();
    }

    private void updateHomeBadge() {
        // Không cần trực tiếp cập nhật trong Activity
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigateToHome", true);
        startActivity(intent);
        finish();
    }
}