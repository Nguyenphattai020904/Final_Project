package com.example.final_project;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtFullName, txtGender, txtDob, txtPhone, txtEmail;
    private ImageButton btnEditName, btnEditGender, btnEditDob, btnEditPhone;
    private Button btnEditProfile;
    private SharedPreferences sharedPreferences;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo các view
        imgAvatar = findViewById(R.id.img_avatar);
        txtFullName = findViewById(R.id.txt_full_name);
        txtGender = findViewById(R.id.txt_gender);
        txtDob = findViewById(R.id.txt_dob);
        txtPhone = findViewById(R.id.txt_phone);
        txtEmail = findViewById(R.id.txt_email);
        btnEditName = findViewById(R.id.btn_edit_name);
        btnEditGender = findViewById(R.id.btn_edit_gender);
        btnEditDob = findViewById(R.id.btn_edit_dob);
        btnEditPhone = findViewById(R.id.btn_edit_phone);
        btnEditProfile = findViewById(R.id.btn_edit_profile);

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Load thông tin người dùng từ SharedPreferences
        loadUserInfo();

        // Xử lý sự kiện chỉnh sửa
        btnEditName.setOnClickListener(v -> showEditDialog(txtFullName, "Enter new name"));
        btnEditGender.setOnClickListener(v -> showGenderDialog());
        btnEditDob.setOnClickListener(v -> showDatePicker());
        btnEditPhone.setOnClickListener(v -> showEditDialog(txtPhone, "Enter new phone number"));

        btnEditProfile.setOnClickListener(v -> {
            if (isEditing) {
                saveProfile();
            } else {
                enableEditing();
            }
        });
    }

    private void loadUserInfo() {
        String token = sharedPreferences.getString("access_token", "");
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.getUserInfo("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    txtFullName.setText(userResponse.getFullname());
                    txtEmail.setText(userResponse.getUserDetails().getEmail());
                    txtPhone.setText(userResponse.getUserDetails().getPhone());
                    txtGender.setText(userResponse.getUserDetails().getGender() != null ? userResponse.getUserDetails().getGender() : "Not set");
                    txtDob.setText(userResponse.getUserDetails().getDateOfBirth() != null ? userResponse.getUserDetails().getDateOfBirth() : "Not set");

                    // Cập nhật lại SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fullname", userResponse.getFullname());
                    editor.putString("email", userResponse.getUserDetails().getEmail());
                    editor.putString("phone", userResponse.getUserDetails().getPhone());
                    editor.putString("gender", userResponse.getUserDetails().getGender());
                    editor.putString("dateOfBirth", userResponse.getUserDetails().getDateOfBirth());
                    editor.apply();

                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(TextView textView, String hint) {
        if (!isEditing) return;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_text);
        EditText edtInput = dialog.findViewById(R.id.edt_input);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        edtInput.setHint(hint);
        edtInput.setText(textView.getText());

        btnSave.setOnClickListener(v -> {
            String newValue = edtInput.getText().toString().trim();
            if (!newValue.isEmpty()) {
                textView.setText(newValue);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showGenderDialog() {
        if (!isEditing) return;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_gender);
        RadioGroup rgGender = dialog.findViewById(R.id.rg_gender);
        RadioButton rbMale = dialog.findViewById(R.id.rb_male);
        RadioButton rbFemale = dialog.findViewById(R.id.rb_female);
        Button btnSaveGender = dialog.findViewById(R.id.btn_save_gender);

        if (txtGender.getText().toString().equals("Male")) {
            rbMale.setChecked(true);
        } else if (txtGender.getText().toString().equals("Female")) {
            rbFemale.setChecked(true);
        }

        btnSaveGender.setOnClickListener(v -> {
            if (rgGender.getCheckedRadioButtonId() == R.id.rb_male) {
                txtGender.setText("Male");
            } else if (rgGender.getCheckedRadioButtonId() == R.id.rb_female) {
                txtGender.setText("Female");
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker() {
        if (!isEditing) return;

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    txtDob.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void enableEditing() {
        isEditing = true;
        btnEditProfile.setText("Save Changes");
        btnEditName.setVisibility(View.VISIBLE);
        btnEditGender.setVisibility(View.VISIBLE);
        btnEditDob.setVisibility(View.VISIBLE);
        btnEditPhone.setVisibility(View.VISIBLE);
    }

    private void saveProfile() {
        String name = txtFullName.getText().toString();
        String gender = txtGender.getText().toString();
        String dob = txtDob.getText().toString();
        String phone = txtPhone.getText().toString();

        UserRequest request = new UserRequest();
        request.setName(name);
        request.setPhone(phone);
        request.setGender(gender.equals("Not set") ? null : gender);
        request.setDateOfBirth(dob.equals("Not set") ? null : dob);

        String token = sharedPreferences.getString("access_token", "");
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.updateProfile("Bearer " + token, request);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    isEditing = false;
                    btnEditProfile.setText("Edit Information");
                    btnEditName.setVisibility(View.GONE);
                    btnEditGender.setVisibility(View.GONE);
                    btnEditDob.setVisibility(View.GONE);
                    btnEditPhone.setVisibility(View.GONE);

                    // Cập nhật lại SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fullname", name);
                    editor.putString("phone", phone);
                    editor.putString("gender", gender.equals("Not set") ? null : gender);
                    editor.putString("dateOfBirth", dob.equals("Not set") ? null : dob);
                    editor.apply();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}