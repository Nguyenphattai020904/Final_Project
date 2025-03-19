package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText edtEmail, edtOTP;
    private Button btnRecover, btnBackToLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // Thay đổi thành layout của bạn

        // Ánh xạ ID
        edtEmail = findViewById(R.id.edt_email);
        edtOTP = findViewById(R.id.edt_otp);
        btnRecover = findViewById(R.id.btn_recover);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        databaseHelper = new DatabaseHelper(this);

        // Xử lý sự kiện nhấn nút "Recover Password"
        btnRecover.setOnClickListener(v -> recoverPassword());

        // Quay lại màn hình đăng nhập
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LogInActivity.class));
            finish();
        });
    }



    private void recoverPassword() {
        String username = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(username) ) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }




   }



    // Kiểm tra email hợp lệ
    private boolean isEmail(String username) {
        return username.contains("@");
    }

    // Kiểm tra số điện thoại hợp lệ
    private boolean isPhoneNumber(String username) {
        // Bạn có thể tùy chỉnh regex này để kiểm tra số điện thoại theo yêu cầu của bạn
        return username.matches("^[0-9]{10}$");
    }
}
