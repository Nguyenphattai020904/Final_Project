package com.example.final_project.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.Log.LogInActivity;
import com.example.final_project.R;

public class OnboardingActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Button btnFinishOnboarding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Khi hoàn thành onboarding, đánh dấu là đã hoàn thành và chuyển sang màn hình chính
        btnFinishOnboarding = findViewById(R.id.btn_start_onboard);
        btnFinishOnboarding.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        // Đánh dấu người dùng đã hoàn thành onboarding
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isOnboarded", true);  // Đánh dấu đã hoàn thành onboarding
        editor.apply();

        // Chuyển sang màn hình đăng nhập
        startActivity(new Intent(OnboardingActivity.this, LogInActivity.class));
        finish();
    }
}
