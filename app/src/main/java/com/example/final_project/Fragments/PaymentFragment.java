package com.example.final_project.Fragments;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.Activity.AddEditAddressActivity;
import com.example.final_project.Address.Address;
import com.example.final_project.Address.District;
import com.example.final_project.Log.LogInActivity;
import com.example.final_project.Products.Product;
import com.example.final_project.R;
import com.example.final_project.Voucher;
import com.example.final_project.Adapter.VoucherSpinnerAdapter;
import com.example.final_project.Address.Province;
import com.example.final_project.Address.Ward;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {
    private EditText edtFullName, edtPhoneNumber;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbZaloPay;
    private Button btnConfirmOrder, btnCancelOrder, btnAddNewAddress;
    private LinearLayout orderItemsContainer, newAddressContainer;
    private TextView txtTotalCost, txtDiscount, txtFinalCost;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard, spinnerSavedAddress, spinnerVouchers;
    private int lastPendingOrderId = -1;
    private ProgressDialog progressDialog;
    private boolean isZaloPayPaymentPending = false;
    private double totalCost = 0;
    private double discount = 0;
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    private List<Address> savedAddressList = new ArrayList<>();
    private List<Product> selectedItems = new ArrayList<>();
    private int maxRetries = 5;
    private int retryCount = 0;

    private static final String KEY_PENDING_ORDER_ID = "lastPendingOrderId";
    private static final String KEY_PAYMENT_PENDING = "isZaloPayPaymentPending";

    private ActivityResultLauncher<Intent> addressLauncher;
    private ActivityResultLauncher<Intent> zaloPayLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        loadSavedAddresses(RetrofitClient.getClient().create(ApiService.class), getUserToken());
                    }
                }
        );

        zaloPayLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (lastPendingOrderId != -1) {
                        isZaloPayPaymentPending = true;
                        retryCount = 0;
                        progressDialog.show();
                        checkOrderStatus(lastPendingOrderId);
                    }
                }
        );

        if (savedInstanceState != null) {
            lastPendingOrderId = savedInstanceState.getInt(KEY_PENDING_ORDER_ID, -1);
            isZaloPayPaymentPending = savedInstanceState.getBoolean(KEY_PAYMENT_PENDING, false);
            Log.d("PaymentFragment", "Restored state: lastPendingOrderId=" + lastPendingOrderId + ", isZaloPayPaymentPending=" + isZaloPayPaymentPending);
        }

        if (getArguments() != null) {
            Serializable serializable = getArguments().getSerializable("selectedItems");
            if (serializable instanceof ArrayList<?>) {
                selectedItems = (ArrayList<Product>) serializable;
            }
            if (selectedItems == null) {
                selectedItems = new ArrayList<>();
            }
        }

        handleDeepLink(requireActivity().getIntent());
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
                    if (progressDialog != null && !progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                    checkOrderStatus(lastPendingOrderId);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment, container, false);

        edtFullName = view.findViewById(R.id.edt_full_name);
        edtPhoneNumber = view.findViewById(R.id.edt_phone_number);
        rgPaymentMethod = view.findViewById(R.id.rg_payment_method);
        rbCOD = view.findViewById(R.id.rb_cod);
        rbZaloPay = view.findViewById(R.id.rb_zalo_pay);
        btnConfirmOrder = view.findViewById(R.id.btn_confirm_order);
        btnCancelOrder = view.findViewById(R.id.btn_cancel_order);
        btnAddNewAddress = view.findViewById(R.id.btn_add_new_address);
        orderItemsContainer = view.findViewById(R.id.order_items_container);
        txtTotalCost = view.findViewById(R.id.txt_total_cost);
        txtDiscount = view.findViewById(R.id.txt_discount);
        txtFinalCost = view.findViewById(R.id.txt_final_cost);
        spinnerSavedAddress = view.findViewById(R.id.spinner_saved_address);
        newAddressContainer = view.findViewById(R.id.new_address_container);
        spinnerProvince = view.findViewById(R.id.spinner_province);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        spinnerWard = view.findViewById(R.id.spinner_ward);
        spinnerVouchers = view.findViewById(R.id.spinner_vouchers);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        setupVoucherSpinner();
        displayOrderItems();
        setupSpinners();

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
        btnCancelOrder.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditAddressActivity.class);
            addressLauncher.launch(intent);
        });

        return view;
    }

    private void setupVoucherSpinner() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String userIdStr = requireActivity().getSharedPreferences("userPrefs", requireContext().MODE_PRIVATE).getString("userId", "0");
        int userId = Integer.parseInt(userIdStr);

        Call<List<Voucher>> call = apiService.getVouchers(userId);
        call.enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Voucher> vouchers = response.body();
                    vouchers.add(0, new Voucher(0, 0, "Chọn voucher", 0, "none", 0, null, 0, null));
                    VoucherSpinnerAdapter adapter = new VoucherSpinnerAdapter(getContext(), vouchers, totalCost);
                    spinnerVouchers.setAdapter(adapter);

                    spinnerVouchers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Voucher selectedVoucher = vouchers.get(position);
                            if (position == 0) {
                                discount = 0;
                                updateCostDisplay();
                                return;
                            }
                            if (selectedVoucher.isApplicable(totalCost)) {
                                applyVoucher(selectedVoucher);
                            } else {
                                Toast.makeText(getContext(), "Đơn hàng không đủ điều kiện cho voucher này", Toast.LENGTH_SHORT).show();
                                spinnerVouchers.setSelection(0);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải voucher: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyVoucher(Voucher voucher) {
        discount = voucher.getVoucherType().equals("percentage") ? totalCost * (voucher.getVoucherValue() / 100) : voucher.getVoucherValue();
        updateCostDisplay();
    }

    private void updateCostDisplay() {
        txtTotalCost.setText(String.format("Tổng tiền: %.0f VND", totalCost));
        txtDiscount.setText(String.format("Giảm giá: %.0f VND", discount));
        txtFinalCost.setText(String.format("Thành tiền: %.0f VND", totalCost - discount));
    }

    private void displayOrderItems() {
        totalCost = 0;
        orderItemsContainer.removeAllViews();

        for (Product product : selectedItems) {
            TextView itemView = new TextView(getContext());
            double priceToUse = product.getFinalPrice();
            itemView.setText(String.format("%s - Số lượng: %d - Giá: %.0f VND",
                    product.getName(), product.getQuantity(), priceToUse));
            itemView.setTextSize(16);
            itemView.setTextColor(getResources().getColor(android.R.color.black));
            itemView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemView.setPadding(0, 0, 0, 8);
            orderItemsContainer.addView(itemView);
            totalCost += priceToUse * product.getQuantity();
            Log.d("PaymentFragment", "Product ID: " + product.getProductId() + ", Price: " + priceToUse + ", Qty: " + product.getQuantity());
        }

        updateCostDisplay();
    }

    private void setupSpinners() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = getUserToken();

        loadSavedAddresses(apiService, token);

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

    private void loadSavedAddresses(ApiService apiService, String token) {
        String userIdStr = requireActivity().getSharedPreferences("userPrefs", requireContext().MODE_PRIVATE).getString("userId", null);
        if (userIdStr == null) {
            Toast.makeText(getContext(), "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LogInActivity.class));
            requireActivity().finish();
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, addressOptions);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSavedAddress.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Không thể tải địa chỉ đã lưu: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("PaymentFragment", "Response code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải địa chỉ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PaymentFragment", "Error: " + t.getMessage());
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, provinceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProvince.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải tỉnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, districtNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải huyện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, wardNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWard.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Ward>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải xã: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isZaloPayPaymentPending && lastPendingOrderId != -1) {
            retryCount = 0;
            progressDialog.show();
            checkOrderStatus(lastPendingOrderId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PENDING_ORDER_ID, lastPendingOrderId);
        outState.putBoolean(KEY_PAYMENT_PENDING, isZaloPayPaymentPending);
    }

    private void confirmOrder() {
        String fullName = edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String paymentMethod = rbCOD.isChecked() ? "COD" : "ZaloPay";
        String address;

        int selectedPosition = spinnerSavedAddress.getSelectedItemPosition();
        if (selectedPosition == 0) {
            if (spinnerProvince.getSelectedItem() == null || spinnerDistrict.getSelectedItem() == null || spinnerWard.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn đầy đủ tỉnh, huyện, xã", Toast.LENGTH_SHORT).show();
                return;
            }
            address = spinnerWard.getSelectedItem() + ", " + spinnerDistrict.getSelectedItem() + ", " + spinnerProvince.getSelectedItem();
        } else {
            address = savedAddressList.get(selectedPosition - 1).toString();
        }

        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItem> items = new ArrayList<>();
        for (Product product : selectedItems) {
            items.add(new OrderRequest.OrderItem(product.getProductId(), product.getQuantity()));
        }

        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LogInActivity.class));
            requireActivity().finish();
            return;
        }

        int voucherId = spinnerVouchers.getSelectedItemPosition() > 0 ?
                ((Voucher) spinnerVouchers.getSelectedItem()).getVoucherId() : 0;

        calculateTotalPrice(token, items, voucherId, paymentMethod, fullName, phoneNumber, address);
    }

    private void calculateTotalPrice(String token, List<OrderRequest.OrderItem> items, int voucherId, String paymentMethod, String fullName, String phoneNumber, String address) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
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
                    Log.d("PaymentFragment", "Server calculated total_price: " + serverTotalPrice);
                    createOrder(token, items, paymentMethod, fullName, phoneNumber, address, serverTotalPrice, voucherId);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Lỗi tính tổng giá: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("PaymentFragment", "Calculate total error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Lỗi kết nối khi tính tổng giá: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PaymentFragment", "Calculate total network error: " + t.getMessage());
            }
        });
    }

    private void createOrder(String token, List<OrderRequest.OrderItem> items, String paymentMethod, String fullName, String phoneNumber, String address, double totalPrice, int voucherId) {
        OrderRequest orderRequest = new OrderRequest(items, paymentMethod, fullName, phoneNumber, address, totalPrice, voucherId);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Log.d("PaymentFragment", "Sending order request: " + orderRequest.toString());

        Call<OrderResponse> call = apiService.createOrder("Bearer " + token, orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    lastPendingOrderId = orderResponse.getPendingOrderId();
                    Log.d("PaymentFragment", "Order Response: " + orderResponse.toString());

                    if (paymentMethod.equals("COD")) {
                        Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        addNotification("Đặt hàng thành công qua COD! Đơn hàng #" + lastPendingOrderId + " đang được xử lý.");
                        navigateBackToHome();
                    } else if (paymentMethod.equals("ZaloPay")) {
                        String zaloPayUrl = orderResponse.getZaloPayUrl();
                        if (zaloPayUrl != null && !zaloPayUrl.isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                            try {
                                zaloPayLauncher.launch(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getContext(), "ZaloPay Sandbox không được cài đặt. Mở trình duyệt.", Toast.LENGTH_LONG).show();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                zaloPayLauncher.launch(browserIntent);
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi: Không nhận được URL ZaloPay từ server", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("PaymentFragment", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PaymentFragment", "Network error: " + t.getMessage());
            }
        });
    }

    public void checkOrderStatus(int pendingOrderId) {
        if (retryCount >= maxRetries) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Hết thời gian chờ thanh toán ZaloPay. Vui lòng kiểm tra trạng thái trong lịch sử.", Toast.LENGTH_LONG).show();
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
                    Log.d("PaymentFragment", "Order status for " + pendingOrderId + ": " + status + ", newOrderId: " + newOrderId);

                    if ("paid".equals(status)) {
                        progressDialog.dismiss();
                        if (newOrderId != null && newOrderId != pendingOrderId) {
                            lastPendingOrderId = newOrderId; // Cập nhật lastPendingOrderId
                            Log.d("PaymentFragment", "Updated lastPendingOrderId to: " + lastPendingOrderId);
                        }
                        Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                        addNotification("Thanh toán thành công qua ZaloPay! Đơn hàng #" + lastPendingOrderId);
                        isZaloPayPaymentPending = false;
                        retryCount = 0;
                        navigateBackToHome();
                    } else if ("waiting_payment".equals(status)) {
                        retryCount++;
                        Log.d("PaymentFragment", "Retry " + retryCount + "/" + maxRetries + " for order " + pendingOrderId);
                        new Handler().postDelayed(() -> checkOrderStatus(pendingOrderId), 2000);
                    } else {
                        // Xử lý các trạng thái khác (unpaid, failed, v.v.)
                        progressDialog.dismiss();
                        Log.w("PaymentFragment", "Unexpected status received: " + status);
                        Toast.makeText(getContext(), "Trạng thái thanh toán không xác định: " + status, Toast.LENGTH_LONG).show();
                        isZaloPayPaymentPending = false;
                        retryCount = 0;
                        navigateBackToHome();
                    }
                } else {
                    // Trường hợp server trả về lỗi (ví dụ: 404 - Order not found)
                    progressDialog.dismiss();
                    String errorMessage = response.code() == 404 ? "Đơn hàng không tồn tại hoặc đã hết hạn" : "Lỗi server: " + response.message();
                    Log.e("PaymentFragment", "Response failed with code: " + response.code() + ", message: " + response.message());
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    isZaloPayPaymentPending = false;
                    retryCount = 0;
                    navigateBackToHome();
                }
            }

            @Override
            public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                retryCount++;
                Log.e("PaymentFragment", "Network error on retry " + retryCount + "/" + maxRetries + ": " + t.getMessage());
                new Handler().postDelayed(() -> checkOrderStatus(pendingOrderId), 2000);
            }
        });
    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userPrefs", requireContext().MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    private void navigateBackToHome() {
        HomeFragment homeFragment = new HomeFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
        homeFragment.updateNotificationBadge();
    }

    public void checkPaymentStatus() {
        if (lastPendingOrderId != -1) {
            checkOrderStatus(lastPendingOrderId);
        } else {
            Log.e("PaymentFragment", "No pending order ID available to check status");
        }
    }

    private void addNotification(String message) {
        updateHomeBadge();
    }

    private void updateHomeBadge() {
        Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).updateNotificationBadge();
        }
    }
}