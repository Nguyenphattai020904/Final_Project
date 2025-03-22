package com.example.final_project.Log;

import android.annotation.SuppressLint;
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
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private Button btnRegister, btnBackToLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        edtFullName = findViewById(R.id.edt_fullname);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        if (edtFullName == null || edtEmail == null || btnRegister == null) {
            Toast.makeText(this, "Error: Missing UI components!", Toast.LENGTH_SHORT).show();
            Log.e("RegisterActivity", "UI components not found");
            return;
        }

        // Toggle password visibility
        setupPasswordVisibility(edtPassword);
        setupPasswordVisibility(edtConfirmPassword);

        // Register action
        btnRegister.setOnClickListener(v -> {
            Log.d("RegisterActivity", "Register button clicked");
            try {
                registerUser();
            } catch (Exception e) {
                Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("RegisterActivity", "Error: ", e);
            }
        });

        // Back to login
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
            finish();
        });
    }

    private void setupPasswordVisibility(EditText editText) {
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                    if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void registerUser() {
        Log.d("RegisterActivity", "Inside registerUser");

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

// Input validations
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

// Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

// Validate phone number format (digits only)
        if (!phone.matches("\\d+")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

// Validate password length
        if (password.length() < 8) {
            Toast.makeText(this, "Mật khẩu phải chứa ít nhất 8 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

// Validate password confirmation
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d("RegisterActivity", "Creating API call");

        UserRequest request = UserRequest.createRegistrationRequest(fullName, email, phone, password);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.registerUser(request);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Email đã tồn tại hoặc lỗi server!", Toast.LENGTH_SHORT).show();
                    Log.e("RegisterError", "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
                Log.e("RegisterError", "Error: " + t.getMessage());
            }
        });
    }
}
