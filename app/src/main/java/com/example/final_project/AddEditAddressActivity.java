package com.example.final_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.Log.LogInActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditAddressActivity extends AppCompatActivity {
    private EditText edtStreetAddress;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private Button btnSave;
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    private int addressId = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        edtStreetAddress = findViewById(R.id.edt_street_address);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerWard = findViewById(R.id.spinner_ward);
        btnSave = findViewById(R.id.btn_save);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Intent intent = getIntent();
        if (intent.hasExtra("address_id")) {
            addressId = intent.getIntExtra("address_id", -1);
            edtStreetAddress.setText(intent.getStringExtra("street_address"));
            // Tải dữ liệu ban đầu cho Spinner (có thể thêm logic chọn sẵn tỉnh, huyện, xã)
        }

        setupSpinners(apiService);

        btnSave.setOnClickListener(v -> saveAddress(apiService));
    }

    private void setupSpinners(ApiService apiService) {
        loadProvinces(apiService);

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!provinceList.isEmpty()) {
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
                if (!districtList.isEmpty()) {
                    District selectedDistrict = districtList.get(position);
                    loadWards(apiService, selectedDistrict.getCode());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditAddressActivity.this, android.R.layout.simple_spinner_item, provinceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProvince.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this, "Lỗi tải tỉnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditAddressActivity.this, android.R.layout.simple_spinner_item, districtNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this, "Lỗi tải huyện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditAddressActivity.this, android.R.layout.simple_spinner_item, wardNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWard.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Ward>> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this, "Lỗi tải xã: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAddress(ApiService apiService) {
        String street_address = edtStreetAddress.getText().toString().trim();
        if (street_address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ cụ thể", Toast.LENGTH_SHORT).show();
            return;
        }

        int provincePosition = spinnerProvince.getSelectedItemPosition();
        int districtPosition = spinnerDistrict.getSelectedItemPosition();
        int wardPosition = spinnerWard.getSelectedItemPosition();

        if (provincePosition < 0 || districtPosition < 0 || wardPosition < 0) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ tỉnh, huyện, xã", Toast.LENGTH_SHORT).show();
            return;
        }

        Province selectedProvince = provinceList.get(provincePosition);
        District selectedDistrict = districtList.get(districtPosition);
        Ward selectedWard = wardList.get(wardPosition);

        String userId = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }

        Address address = new Address(
                addressId,
                Integer.parseInt(userId),
                selectedProvince.getCode(),
                selectedProvince.getName(),
                selectedDistrict.getCode(),
                selectedDistrict.getName(),
                selectedWard.getCode(),
                selectedWard.getName(),
                street_address,
                false
        );

        Call<Void> call = apiService.saveAddress(address);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditAddressActivity.this, "Lưu địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddEditAddressActivity.this, "Lỗi khi lưu địa chỉ: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddEditAddressActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}