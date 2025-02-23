package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText edtUsername, edtSecurityQuestion, edtSecurityAnswer;
    private Button btnRecover, btnBackToLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // Thay đổi thành layout của bạn

        // Ánh xạ ID
        edtUsername = findViewById(R.id.txt_username);
        edtSecurityQuestion = findViewById(R.id.txt_security_question);
        edtSecurityAnswer = findViewById(R.id.txt_security_answer);
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
        String username = edtUsername.getText().toString().trim();
        String securityQuestion = edtSecurityQuestion.getText().toString().trim();
        String securityAnswer = edtSecurityAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(securityQuestion) || TextUtils.isEmpty(securityAnswer)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bỏ việc mã hóa ở đây
        if (isEmail(username)) {
            if (databaseHelper.isEmailExists(username)) {
                checkSecurityQuestion(username, securityQuestion, securityAnswer);
            } else {
                Toast.makeText(this, "Email không tồn tại!", Toast.LENGTH_SHORT).show();
            }
        } else if (isPhoneNumber(username)) {
            if (databaseHelper.isPhoneExists(username)) {
                checkSecurityQuestion(username, securityQuestion, securityAnswer);
            } else {
                Toast.makeText(this, "Số điện thoại không tồn tại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Email hoặc số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkSecurityQuestion(String username, String securityQuestion, String securityAnswer) {
        if (databaseHelper.checkSecurityQuestionAnswer(username, securityQuestion, securityAnswer)) {
            Intent intent = new Intent(ForgotPasswordActivity.this, NewPassWordActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Câu trả lời bảo mật không đúng!", Toast.LENGTH_SHORT).show();
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
