package com.example.final_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtPhone, edtPassword, edtConfirmPassword, edtSecurityQuestion, edtSecurityAnswer;
    private Button btnRegister, btnBackToLogin;
    private DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ ID
        edtFullName = findViewById(R.id.edt_fullname);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone_number);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        databaseHelper = new DatabaseHelper(this);

        // Xử lý hiển thị/ẩn mật khẩu
        handlePasswordVisibility(edtPassword);
        handlePasswordVisibility(edtConfirmPassword);

        // Xử lý đăng ký
        btnRegister.setOnClickListener(v -> registerUser());

        // Quay lại màn hình đăng nhập
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
            finish();
        });
    }

    private void handlePasswordVisibility(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            return false;
        });
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String securityQuestion = edtSecurityQuestion.getText().toString().trim();
        String securityAnswer = edtSecurityAnswer.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || securityQuestion.isEmpty() || securityAnswer.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.isEmailExists(email)) {
            Toast.makeText(this, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mã hóa câu trả lời bảo mật trước khi lưu
        String hashedSecurityAnswer = SecurityUtils.hashSHA256(securityAnswer);
        // Thêm user vào database với câu trả lời bảo mật đã mã hóa
        boolean success = databaseHelper.addUser(fullName, email, phone, password, securityQuestion, securityAnswer);

        if (success) {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

}
