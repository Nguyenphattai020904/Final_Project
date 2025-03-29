package com.example.final_project.Log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.MainActivity;
import com.example.final_project.R;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private TextView txtSignUp, txtForgotPassword;
    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnGoogleLogin;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int GOOGLE_SIGN_IN_REQUEST = 100;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.txt_username);
        edtPassword = findViewById(R.id.txt_password);
        btnLogin = findViewById(R.id.btn_log);
        btnGoogleLogin = findViewById(R.id.btn_gg);
        txtSignUp = findViewById(R.id.txt_sign_up);
        txtForgotPassword = findViewById(R.id.txt_forgot_password);

        firebaseAuth = FirebaseAuth.getInstance();

        // Xử lý hiển thị/ẩn mật khẩu bằng icon con mắt
        setupPasswordVisibility(edtPassword);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> loginUser());
        txtSignUp.setOnClickListener(v -> startActivity(new Intent(LogInActivity.this, RegisterActivity.class)));
        txtForgotPassword.setOnClickListener(v -> startActivity(new Intent(LogInActivity.this, ForgotPasswordActivity.class)));
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void setupPasswordVisibility(EditText editText) {
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                    if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void loginUser() {
        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        UserRequest loginRequest = UserRequest.createLoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    String userId = response.body().getUserId();
                    UserResponse.UserDetails userDetails = response.body().getUserDetails();

                    Log.d("LoginDebug", "Token from API: " + token);
                    Log.d("LoginDebug", "User ID from API: " + userId);
                    Log.d("LoginDebug", "User Details: " + (userDetails != null ? userDetails.getName() : "null"));

                    if (token == null || token.isEmpty() || userId == null || userId.isEmpty()) {
                        Log.e("LoginError", "Token or userId is null or empty");
                        Toast.makeText(LogInActivity.this, "Đăng nhập thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token", token);
                    editor.putString("userId", userId);
                    editor.putString("fullname", userDetails != null ? userDetails.getName() : response.body().getFullname());
                    if (userDetails != null) {
                        editor.putString("email", userDetails.getEmail());
                        editor.putString("phone", userDetails.getPhone());
                        editor.putString("gender", userDetails.getGender());
                        editor.putString("dateOfBirth", userDetails.getDateOfBirth());
                    }
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    // Kiểm tra xem token và userId có được lưu không
                    String savedToken = sharedPreferences.getString("access_token", null);
                    String savedUserId = sharedPreferences.getString("userId", null);
                    Log.d("LoginDebug", "Token saved in SharedPreferences: " + savedToken);
                    Log.d("LoginDebug", "UserId saved in SharedPreferences: " + savedUserId);

                    startActivity(new Intent(LogInActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LogInActivity.this, "Sai thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
                    Log.e("LoginError", "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LogInActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
                Log.e("LoginError", "Error: " + t.getMessage());
            }
        });
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
                Log.e("GoogleLogin", "Đăng nhập Google thất bại!", e);
                Toast.makeText(this, "Đăng nhập Google thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "Lỗi khi lấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
                            Log.e("GoogleLogin", "FirebaseUser is null");
                            return;
                        }

                        String fullName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                        String email = user.getEmail();

                        // Gọi API để lấy hoặc tạo userId dựa trên email
                        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                        UserRequest userRequest = new UserRequest();
                        userRequest.setEmail(email);
                        userRequest.setName(fullName);

                        apiService.registerUser(userRequest).enqueue(new Callback<UserResponse>() {
                            @Override
                            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    String userId = response.body().getUserId();
                                    String token = response.body().getToken();

                                    if (userId == null || userId.isEmpty()) {
                                        Toast.makeText(LogInActivity.this, "Không thể lấy userId!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("access_token", token != null ? token : idToken);
                                    editor.putString("userId", userId);
                                    editor.putString("fullname", fullName);
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.apply();

                                    String savedToken = sharedPreferences.getString("access_token", null);
                                    String savedUserId = sharedPreferences.getString("userId", null);
                                    Log.d("GoogleLogin", "Token saved in SharedPreferences: " + savedToken);
                                    Log.d("GoogleLogin", "UserId saved in SharedPreferences: " + savedUserId);
                                    Log.d("GoogleLogin", "Đăng nhập Google thành công: " + fullName);

                                    startActivity(new Intent(LogInActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LogInActivity.this, "Không thể đăng ký user Google!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<UserResponse> call, Throwable t) {
                                Toast.makeText(LogInActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e("GoogleLogin", "Firebase authentication failed");
                        Toast.makeText(this, "Xác thực với Firebase thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}