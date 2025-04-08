package com.example.final_project;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LuckyWheel extends AppCompatActivity {
    private ImageView wheelImage, arrowImage;
    private Button spinButton, backButton;
    private TextView spinCountText, errorMessageText;
    private int userId;
    private String token;
    private int spinCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lucky_wheel);

        wheelImage = findViewById(R.id.wheel_image);
        arrowImage = findViewById(R.id.arrow_image);
        spinButton = findViewById(R.id.spin_button);
        backButton = findViewById(R.id.button_back);
        spinCountText = findViewById(R.id.spin_count_text);
        errorMessageText = findViewById(R.id.error_message_text);

        userId = Integer.parseInt(getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", "0"));
        token = getSharedPreferences("userPrefs", MODE_PRIVATE).getString("access_token", "");

        loadSpinCount();

        spinButton.setOnClickListener(v -> spinWheel());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadSpinCount() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Map<String, Integer>> call = apiService.getSpinCount(userId, "Bearer " + token);
        call.enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spinCount = response.body().get("spinCount");
                    spinCountText.setText("Lượt quay: " + spinCount);
                    spinButton.setEnabled(spinCount > 0);
                    // Ẩn thông báo lỗi nếu có lượt quay
                    if (spinCount > 0) {
                        errorMessageText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(LuckyWheel.this, "Lỗi tải lượt quay", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                Toast.makeText(LuckyWheel.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void spinWheel() {
        if (spinCount <= 0) {
            errorMessageText.setText("Bạn đã hết lượt quay!");
            errorMessageText.setVisibility(View.VISIBLE);
            return;
        }

        // Ẩn thông báo lỗi khi bắt đầu quay
        errorMessageText.setVisibility(View.GONE);
        spinButton.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Map<String, String>> call = apiService.spinWheel(userId, "Bearer " + token);
        System.out.println("Calling POST: http://192.168.1.117:3000/api/spin/spin/" + userId);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().get("result");
                    float targetAngle = calculateTargetAngle(result);
                    float totalAngle = 5 * 360 + targetAngle;

                    ObjectAnimator wheelAnimator = ObjectAnimator.ofFloat(wheelImage, "rotation", 0f, totalAngle);
                    wheelAnimator.setDuration(5000);
                    wheelAnimator.setInterpolator(new DecelerateInterpolator());
                    wheelAnimator.start();

                    wheelAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ObjectAnimator arrowAnimator = ObjectAnimator.ofFloat(arrowImage, "translationY", 0f, -10f, 0f);
                            arrowAnimator.setDuration(300);
                            arrowAnimator.setRepeatCount(2);
                            arrowAnimator.start();

                            Toast.makeText(LuckyWheel.this, result, Toast.LENGTH_LONG).show();
                            loadSpinCount();
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {}

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });
                } else {
                    if (response.code() == 400 && response.errorBody() != null) {
                        try {
                            String errorMessage = response.errorBody().string();
                            if (errorMessage.contains("Bạn đã hết lượt quay!")) {
                                errorMessageText.setText("Bạn đã hết lượt quay!");
                                errorMessageText.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(LuckyWheel.this, "Lỗi quay: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(LuckyWheel.this, "Lỗi quay: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LuckyWheel.this, "Lỗi quay: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                    spinButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(LuckyWheel.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                spinButton.setEnabled(true);
            }
        });
    }

    // Hàm tính góc dừng dựa trên kết quả từ API
    private float calculateTargetAngle(String result) {
        if (result.contains("Chúc bạn may mắn")) {
            // Chọn ngẫu nhiên một trong các góc "Chúc bạn may mắn"
            float[] noRewardAngles = {54, 126, 198, 270, 342}; // Góc giữa của các phần "Chúc bạn may mắn"
            int randomIndex = (int) (Math.random() * noRewardAngles.length);
            return noRewardAngles[randomIndex];
        }

        // Lấy giá trị discount từ chuỗi (ví dụ: "10%", "20%", v.v.)
        String[] parts = result.split(" ");
        String discountStr = parts[parts.length - 1]; // Lấy phần cuối (ví dụ: "20%")
        int discount = Integer.parseInt(discountStr.replace("%", "")); // Chuyển thành số (10, 20, v.v.)

        // Điều chỉnh ánh xạ góc để khớp với thiết kế vòng quay (10 phần xen kẽ)
        switch (discount) {
            case 10:
                return 18;  // Phần thưởng: 0° - 36° (giữa là 18°)
            case 20:
                return 90;  // Phần thưởng: 72° - 108° (giữa là 90°)
            case 30:
                return 162; // Phần thưởng: 144° - 180° (giữa là 162°)
            case 40:
                return 234; // Phần thưởng: 216° - 252° (giữa là 234°)
            case 50:
                return 306; // Phần thưởng: 288° - 324° (giữa là 306°)
            default:
                return (float) (Math.random() * 360); // Nếu không xác định được, quay ngẫu nhiên
        }
    }
}