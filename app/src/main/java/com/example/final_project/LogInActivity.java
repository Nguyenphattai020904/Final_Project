package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogInActivity extends AppCompatActivity {

    private TextView txtSignUp, txtForgotPassword;
    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnGoogleLogin;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int GOOGLE_SIGN_IN_REQUEST = 100;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ ID
        edtUsername = findViewById(R.id.txt_username);
        edtPassword = findViewById(R.id.txt_password);
        btnLogin = findViewById(R.id.btn_log);
        btnGoogleLogin = findViewById(R.id.btn_gg); // Button đăng nhập bằng Google
        txtSignUp = findViewById(R.id.txt_sign_up);
        txtForgotPassword = findViewById(R.id.txt_forgot_password);

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Client ID lấy từ strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Xử lý hiển thị/ẩn mật khẩu
        handlePasswordVisibility(edtPassword);

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> loginUser());

        // Chuyển sang màn hình đăng ký
        txtSignUp.setOnClickListener(v -> startActivity(new Intent(LogInActivity.this, RegisterActivity.class)));

        // Chuyển sang màn hình quên mật khẩu
        txtForgotPassword.setOnClickListener(v -> startActivity(new Intent(LogInActivity.this, ForgotPasswordActivity.class)));

        // Đăng nhập bằng Google
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
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

    private void loginUser() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Kiểm tra xem username và password có để trống không
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra thông tin đăng nhập với database
        boolean isAuthenticated = databaseHelper.authenticateUser(username, password);

        if (isAuthenticated) {
            // Lấy tên đầy đủ từ cơ sở dữ liệu
            String fullName = databaseHelper.getUserFullName(username);

            // Đăng nhập thành công
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Lưu thông tin người dùng vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fullName", fullName);  // Lưu tên đầy đủ
            editor.putBoolean("isLoggedIn", true);   // Đánh dấu đã đăng nhập
            editor.apply();

            // Chuyển sang màn hình chính
            startActivity(new Intent(LogInActivity.this, MainActivity.class));
            finish();
        } else {
            // Đăng nhập thất bại
            Toast.makeText(this, "Sai thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }


    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Xác thực với Firebase bằng Google ID Token
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(this, "Đăng nhập Google thành công: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // Lưu thông tin người dùng vào SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("fullName", user.getDisplayName());
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        startActivity(new Intent(LogInActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Xác thực với Firebase thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
