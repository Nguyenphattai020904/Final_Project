package com.example.final_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Requests.FeedbackRequest;
import com.example.final_project.Fragments.ProfileFragment;
import com.example.final_project.Log.LogInActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {
    private TextView tvEmail, tvPhone;
    private EditText etMessage;
    private Button btnSendFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        etMessage = findViewById(R.id.etMessage);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);

        // Sự kiện click email
        tvEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:freshfoodtuoisach2025@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback from FreshFood App");
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện click phone
        tvPhone.setOnClickListener(v -> {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:0916303763"));
            try {
                startActivity(phoneIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng gọi điện", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện gửi feedback
        btnSendFeedback.setOnClickListener(v -> sendFeedback());
    }

    private void sendFeedback() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        String userIdStr = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", null);
        String name = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("fullname", "Unknown User");
        if (userIdStr == null) {
            Toast.makeText(this, "Không tìm thấy userId, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            finish();
            return;
        }
        int userId = Integer.parseInt(userIdStr);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        FeedbackRequest request = new FeedbackRequest(userId, name, message);
        Call<Void> call = apiService.sendFeedback(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Gửi feedback thành công", Toast.LENGTH_SHORT).show();
                    etMessage.setText("");
                } else {
                    Toast.makeText(FeedbackActivity.this, "Lỗi khi gửi feedback: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(FeedbackActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}