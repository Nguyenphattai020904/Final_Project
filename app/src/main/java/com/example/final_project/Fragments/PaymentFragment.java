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

import androidx.fragment.app.Fragment;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.AddEditAddressActivity;
import com.example.final_project.Address;
import com.example.final_project.AddressManagementActivity;
import com.example.final_project.District;
import com.example.final_project.Log.LogInActivity;
import com.example.final_project.Products.Product;
import com.example.final_project.Province;
import com.example.final_project.R;
import com.example.final_project.Ward;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {
    private EditText edtFullName, edtPhoneNumber, edtStreetAddress;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbZaloPay;
    private Button btnConfirmOrder, btnCancelOrder, btnAddNewAddress;
    private LinearLayout orderItemsContainer, newAddressContainer;
    private TextView txtTotalCost;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard, spinnerSavedAddress;
    private int lastPendingOrderId = -1;
    private ProgressDialog progressDialog;
    private boolean isZaloPayPaymentPending = false;

    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    private List<Address> savedAddressList = new ArrayList<>();

    private static final String KEY_PENDING_ORDER_ID = "lastPendingOrderId";
    private static final String KEY_PAYMENT_PENDING = "isZaloPayPaymentPending";
    private static final int REQUEST_ADD_ADDRESS = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastPendingOrderId = savedInstanceState.getInt(KEY_PENDING_ORDER_ID, -1);
            isZaloPayPaymentPending = savedInstanceState.getBoolean(KEY_PAYMENT_PENDING, false);
            Log.d("PaymentFragment", "Restored state: lastPendingOrderId=" + lastPendingOrderId + ", isZaloPayPaymentPending=" + isZaloPayPaymentPending);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment, container, false);

        // Ánh xạ các thành phần
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
        spinnerSavedAddress = view.findViewById(R.id.spinner_saved_address);
        newAddressContainer = view.findViewById(R.id.new_address_container);
        spinnerProvince = view.findViewById(R.id.spinner_province);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        spinnerWard = view.findViewById(R.id.spinner_ward);
        edtStreetAddress = view.findViewById(R.id.edt_street_address);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        displayOrderItems();
        setupSpinners();

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
        btnCancelOrder.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditAddressActivity.class);
            startActivityForResult(intent, REQUEST_ADD_ADDRESS);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isZaloPayPaymentPending && lastPendingOrderId != -1) {
            checkOrderStatus(lastPendingOrderId);
            isZaloPayPaymentPending = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PENDING_ORDER_ID, lastPendingOrderId);
        outState.putBoolean(KEY_PAYMENT_PENDING, isZaloPayPaymentPending);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ADDRESS && resultCode == getActivity().RESULT_OK) {
            loadSavedAddresses(RetrofitClient.getClient().create(ApiService.class), getUserToken());
        } else if (requestCode == 1 && lastPendingOrderId != -1) {
            isZaloPayPaymentPending = false;
            checkOrderStatus(lastPendingOrderId);
        }
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

    private void setupSpinners() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = getUserToken();

        // Tải danh sách địa chỉ đã lưu
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
        String userIdStr = getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("userId", null);
        if (userIdStr == null) {
            Toast.makeText(getContext(), "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LogInActivity.class)); // Chuyển đến LogInActivity nếu cần
            getActivity().finish();
            return;
        }
        int userId = Integer.parseInt(userIdStr); // Chuyển từ String sang int nếu API yêu cầu
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, addressOptions);
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, provinceNames);
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, districtNames);
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, wardNames);
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

    private void confirmOrder() {
        String fullName = edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String paymentMethod = rbCOD.isChecked() ? "COD" : "ZaloPay";
        String address;

        int selectedPosition = spinnerSavedAddress.getSelectedItemPosition();
        if (selectedPosition == 0) { // Chưa chọn địa chỉ
            Toast.makeText(getContext(), "Vui lòng chọn một địa chỉ đã lưu hoặc thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
            return;
        } else { // Chọn địa chỉ đã lưu
            address = savedAddressList.get(selectedPosition - 1).toString();
        }

        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
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
                    lastPendingOrderId = orderResponse.getPendingOrderId();
                    if (paymentMethod.equals("COD")) {
                        removeSelectedItems(selectedItems);
                        cartFragment.updateCartFromManager();
                        Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                        navigateBackToHome();
                    } else if (paymentMethod.equals("ZaloPay")) {
                        String zaloPayUrl = orderResponse.getZaloPayUrl();
                        if (zaloPayUrl != null && !zaloPayUrl.isEmpty()) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                intent.setPackage("com.zing.zalo.sandbox");
                                startActivityForResult(intent, 1);
                                isZaloPayPaymentPending = true;
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getContext(), "ZaloPay Sandbox không được cài đặt, mở trình duyệt...", Toast.LENGTH_SHORT).show();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zaloPayUrl));
                                startActivity(browserIntent);
                                isZaloPayPaymentPending = true;
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi: Không nhận được URL ZaloPay", Toast.LENGTH_SHORT).show();
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
        return sharedPreferences.getString("access_token", null);
    }

    private void removeSelectedItems(List<Product> selectedItems) {
        CartManager.getInstance().getCartItems().removeAll(selectedItems);
    }

    private void navigateBackToHome() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void checkOrderStatus(int pendingOrderId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        progressDialog.show();
        Call<OrderStatusResponse> call = apiService.checkOrderStatus(pendingOrderId);
        call.enqueue(new Callback<OrderStatusResponse>() {
            @Override
            public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getPaymentStatus();
                    if ("paid".equals(status)) {
                        CartFragment cartFragment = (CartFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CartFragment");
                        if (cartFragment != null) {
                            removeSelectedItems(cartFragment.getSelectedItems());
                            cartFragment.updateCartFromManager();
                        }
                        Toast.makeText(getContext(), "Thanh toán ZaloPay thành công!", Toast.LENGTH_LONG).show();
                        navigateBackToHome();
                    } else {
                        Toast.makeText(getContext(), "Thanh toán ZaloPay chưa hoàn tất", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}