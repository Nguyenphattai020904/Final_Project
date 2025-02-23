package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtFullName;
    private Button btnLogout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Kiểm tra nếu người dùng chưa hoàn thành onboarding
        boolean isOnboarded = sharedPreferences.getBoolean("isOnboarded", false);
        if (!isOnboarded) {
            // Nếu chưa hoàn thành onboarding, chuyển về màn hình onboarding
            startActivity(new Intent(MainActivity.this, OnboardingActivity.class));
            finish();
            return;
        }

        // Kiểm tra nếu người dùng chưa đăng nhập, chuyển về màn hình đăng nhập
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
            finish();
            return;
        }

        // Lấy tên người dùng từ SharedPreferences
        String fullName = sharedPreferences.getString("fullName", "User");
        txtFullName = findViewById(R.id.txt_full_name);
        txtFullName.setText(fullName);  // Hiển thị tên đầy đủ người dùng

        // Xử lý sự kiện đăng xuất
        btnLogout = findViewById(R.id.btn_log_out);
        btnLogout.setOnClickListener(v -> logoutUser());  // Gọi hàm logoutUser khi nhấn nút
    }

    // Hàm xử lý đăng xuất
    private void logoutUser() {
        // Xóa trạng thái đăng nhập khỏi SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);  // Đánh dấu là chưa đăng nhập
        editor.putString("fullName", "");  // Xóa tên người dùng
        editor.apply();

        // Chuyển về màn hình đăng nhập sau khi logout
        startActivity(new Intent(MainActivity.this, LogInActivity.class));
        finish();
    }
}
