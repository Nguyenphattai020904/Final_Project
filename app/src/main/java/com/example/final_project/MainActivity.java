package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//public class MainActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Kiểm tra xem người dùng đã hoàn thành onboarding chưa
//        SharedPreferences preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
//        boolean onboardingComplete = preferences.getBoolean("onboarding_complete", false);
//
//        if (!onboardingComplete) {
//            // Chuyển đến OnboardingActivity nếu chưa hoàn thành onboarding
//            Intent intent = new Intent(this, OnboardingActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }
//
//        // Thiết lập layout chính nếu đã hoàn thành onboarding
//        setContentView(R.layout.activity_main);
//    }
//}


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển đến OnboardingActivity mỗi khi ứng dụng mở
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }
}

