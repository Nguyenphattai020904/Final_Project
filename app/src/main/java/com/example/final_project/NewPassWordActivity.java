package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewPassWordActivity extends AppCompatActivity {
    private EditText edtNewPassword, edtConfirmNewPassword;
    private Button btnSetNewPassword, btnBackToLogin;
    private String username;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        // Ánh xạ ID
        edtNewPassword = findViewById(R.id.txt_new_password);
        edtConfirmNewPassword = findViewById(R.id.txt_confirm_new_password);
        btnSetNewPassword = findViewById(R.id.btn_set_new_password);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        username = getIntent().getStringExtra("username");
        databaseHelper = new DatabaseHelper(this);

        // Xử lý hiển thị/ẩn mật khẩu
        handlePasswordVisibility(edtNewPassword);
        handlePasswordVisibility(edtConfirmNewPassword);

        // Xử lý sự kiện nhấn nút "Set New Password"
        btnSetNewPassword.setOnClickListener(v -> setNewPassword());

        // Quay lại màn hình đăng nhập
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(NewPassWordActivity.this, LogInActivity.class));
            finish();
        });
    }

    private void handlePasswordVisibility(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                if(event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Khi nhấn giữ - hiện password
                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    return true;
                }
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
                // Khi thả tay - ẩn password
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                return true;
            }
            return false;
        });
    }

    private void setNewPassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        if (newPassword.isEmpty() && confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mã hóa mật khẩu mới
        String hashedNewPassword = SecurityUtils.hashSHA256(newPassword);

        // Cập nhật mật khẩu trong cơ sở dữ liệu
        boolean isUpdated = databaseHelper.updatePassword(username, hashedNewPassword);

        if (isUpdated) {
            Toast.makeText(this, "Mật khẩu đã được thay đổi thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(NewPassWordActivity.this, LogInActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Cập nhật mật khẩu thất bại! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}