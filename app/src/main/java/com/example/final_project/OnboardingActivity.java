package com.example.final_project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

//public class OnboardingActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_onboarding);
//
//        Button startButton = findViewById(R.id.btn_start_onboard);
//        startButton.setOnClickListener(v -> {
//            // Lưu trạng thái đã hoàn thành onboarding
//            SharedPreferences preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putBoolean("onboarding_complete", true);
//            editor.apply();
//
//            // Chuyển đến MainActivity sau khi onboarding hoàn tất
//            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        });
//    }
//}

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button startButton = findViewById(R.id.btn_start_onboard);
        startButton.setOnClickListener(v -> {
            // Chuyển đến MainActivity hoặc LoginActivity
            Intent intent = new Intent(OnboardingActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        });
    }
}


