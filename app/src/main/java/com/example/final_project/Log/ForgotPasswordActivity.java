package com.example.final_project.Log;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText edtEmail, edtOTP;
    private Button btnSendOTP, btnVerifyOTP, btnBackToLogin;
    private String email;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edt_email);
        edtOTP = findViewById(R.id.edt_otp);
        btnSendOTP = findViewById(R.id.btn_send_otp);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnSendOTP.setOnClickListener(v -> sendOTP());
        btnVerifyOTP.setOnClickListener(v -> verifyOTP());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LogInActivity.class));
            finish();
        });
    }

    private void saveEmailToPreferences(String email) {
        Log.d("SaveEmail", "Saving email: " + email); // Debug email saving
        getSharedPreferences("forgot_password_prefs", MODE_PRIVATE)
                .edit()
                .putString("email", email)
                .apply();
    }

    private String getEmailFromPreferences() {
        String savedEmail = getSharedPreferences("forgot_password_prefs", MODE_PRIVATE)
                .getString("email", "");
        Log.d("RetrieveEmail", "Retrieved email: " + savedEmail); // Debug email retrieval
        return savedEmail;
    }


    // Update sendOTP to save email
    private void sendOTP() {
        email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save email persistently
        saveEmailToPreferences(email);

        UserRequest request = UserRequest.createOtpRequest(email, null);
        apiService.sendOTP(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ForgotPasswordActivity.this, "OTP đã được gửi đến email!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi gửi OTP!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối API!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Update verifyOTP to retrieve email from preferences
    private void verifyOTP() {
        email = getEmailFromPreferences(); // Retrieve saved email
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập lại email để tiếp tục!", Toast.LENGTH_SHORT).show();
            return;
        }

        String otp = edtOTP.getText().toString().trim();
        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "Vui lòng nhập OTP!", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRequest request = UserRequest.createOtpRequest(email, otp);
        apiService.verifyOTP(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMessage().equals("OTP verified successfully")) {
                    Intent intent = new Intent(ForgotPasswordActivity.this, NewPassWordActivity.class);
                    intent.putExtra("email", email); // Pass the email to the next activity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "OTP không hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi xác thực OTP!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
