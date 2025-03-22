package com.example.final_project.Log;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.R;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPassWordActivity extends AppCompatActivity {
    private EditText edtNewPassword, edtConfirmNewPassword;
    private Button btnSetNewPassword, btnBackToLogin;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        edtNewPassword = findViewById(R.id.txt_new_password);
        edtConfirmNewPassword = findViewById(R.id.txt_confirm_new_password);
        btnSetNewPassword = findViewById(R.id.btn_set_new_password);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        // Retrieve email passed from the previous activity
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        handlePasswordVisibility(edtNewPassword);
        handlePasswordVisibility(edtConfirmNewPassword);

        btnSetNewPassword.setOnClickListener(v -> setNewPassword());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(NewPassWordActivity.this, LogInActivity.class));
            finish();
        });
    }

    private void handlePasswordVisibility(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // Index for the right drawable
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                return true;
            }
            return false;
        });
    }

    private void setNewPassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        // Validate input
        if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log password change request for debugging
        Log.d("NewPasswordActivity", "Email: " + email + ", New Password: " + newPassword);

        // Create UserRequest object
        UserRequest resetRequest = UserRequest.createPasswordResetRequest(email, newPassword);

        // Initialize Retrofit
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Log the request to ensure fields are populated correctly
        Log.d("NewPasswordActivity", "Request Payload: " + new Gson().toJson(resetRequest));

        // Make API call
        apiService.updatePassword(resetRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();

                    // Check if the response contains success message or status code indicating success
                    if ("Password updated successfully".equals(userResponse.getMessage())) {
                        // Password update was successful
                        Toast.makeText(NewPassWordActivity.this, "Mật khẩu đã được thay đổi thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(NewPassWordActivity.this, LogInActivity.class));
                        finish();
                    } else {
                        // Handle failure response
                        Toast.makeText(NewPassWordActivity.this, "Cập nhật mật khẩu thất bại: " + userResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("SetNewPassword", "Backend Error: " + userResponse.getMessage());
                    }
                } else {
                    // Handle API error response
                    try {
                        String errorResponse = response.errorBody().string();
                        Log.e("SetNewPassword", "Error Response: " + errorResponse);
                        Toast.makeText(NewPassWordActivity.this, "Cập nhật mật khẩu thất bại: " + errorResponse, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("SetNewPassword", "Error parsing response", e);
                        Toast.makeText(NewPassWordActivity.this, "Lỗi không xác định!", Toast.LENGTH_SHORT).show();
                    }
                }
            }




            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(NewPassWordActivity.this, "Lỗi kết nối API!", Toast.LENGTH_SHORT).show();
                Log.e("NewPasswordActivity", "API Error: " + t.getMessage());
            }
        });
    }

}
