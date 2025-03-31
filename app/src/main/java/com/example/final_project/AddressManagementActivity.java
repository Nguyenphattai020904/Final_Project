package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.Log.LogInActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAddresses;
    private Button btnAddNewAddress;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_management);

        recyclerViewAddresses = findViewById(R.id.recycler_view_addresses);
        btnAddNewAddress = findViewById(R.id.btn_add_new_address);

        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(addressList, this::onEditAddress, this::onDeleteAddress);
        recyclerViewAddresses.setAdapter(addressAdapter);

        loadAddresses();

        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(AddressManagementActivity.this, AddEditAddressActivity.class);
            startActivity(intent);
        });
    }

    private void loadAddresses() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String userId = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }
        Call<List<Address>> call = apiService.getUserAddresses(Integer.parseInt(userId));
        call.enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addressList.clear();
                    addressList.addAll(response.body());
                    addressAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AddressManagementActivity.this, "Lỗi tải địa chỉ: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                Toast.makeText(AddressManagementActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onEditAddress(Address address) {
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        intent.putExtra("address_id", address.getId());
        intent.putExtra("street_address", address.getStreetAddress());
        startActivity(intent);
    }

    private void onDeleteAddress(Address address) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteAddress(address.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    addressList.remove(address);
                    addressAdapter.notifyDataSetChanged();
                    Toast.makeText(AddressManagementActivity.this, "Xóa địa chỉ thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddressManagementActivity.this, "Lỗi khi xóa địa chỉ: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddressManagementActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses(); // Tải lại danh sách khi quay lại từ AddEditAddressActivity
    }
}