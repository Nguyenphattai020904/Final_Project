package com.example.final_project;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtFullName, txtGender, txtDob, txtPhone, txtEmail;
    private ImageButton btnEditName, btnEditGender, btnEditDob, btnEditPhone;
    private Button btnEditProfile;
    private SharedPreferences sharedPreferences;
    private boolean isEditing = false;
    private Uri selectedImageUri;
    private Uri tempImageUri;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Yêu cầu quyền
        requestPermissions();

        // Khởi tạo ExecutorService để chạy các tác vụ nặng
        executorService = Executors.newFixedThreadPool(2);

        // Khởi tạo các view
        imgAvatar = findViewById(R.id.img_avatar);
        txtFullName = findViewById(R.id.txt_full_name);
        txtGender = findViewById(R.id.txt_gender);
        txtDob = findViewById(R.id.txt_dob);
        txtPhone = findViewById(R.id.txt_phone);
        txtEmail = findViewById(R.id.txt_email);
        btnEditName = findViewById(R.id.btn_edit_name);
        btnEditGender = findViewById(R.id.btn_edit_gender);
        btnEditDob = findViewById(R.id.btn_edit_dob);
        btnEditPhone = findViewById(R.id.btn_edit_phone);
        btnEditProfile = findViewById(R.id.btn_edit_profile);

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Tạo file tạm để lưu ảnh chụp
        File tempFile = new File(getCacheDir(), "temp_image.jpg");
        tempImageUri = FileProvider.getUriForFile(this, "com.example.final_project.fileprovider", tempFile);

        // Khởi tạo ActivityResultLauncher để chọn ảnh từ thư viện
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                Log.d("ProfileActivity", "Image picked: " + selectedImageUri);
                Glide.with(this).load(selectedImageUri).circleCrop().into(imgAvatar);
            }
        });

        // Khởi tạo ActivityResultLauncher để chụp ảnh
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                selectedImageUri = tempImageUri;
                Log.d("ProfileActivity", "Image captured: " + selectedImageUri);
                Glide.with(this).load(selectedImageUri).circleCrop().into(imgAvatar);
            }
        });

        // Load thông tin người dùng
        loadUserInfo();

        // Xử lý sự kiện chỉnh sửa
        btnEditName.setOnClickListener(v -> showEditDialog(txtFullName, "Enter new name"));
        btnEditGender.setOnClickListener(v -> showGenderDialog());
        btnEditDob.setOnClickListener(v -> showDatePicker());
        btnEditPhone.setOnClickListener(v -> showEditDialog(txtPhone, "Enter new phone number"));

        // Xử lý sự kiện chọn ảnh khi nhấn vào imgAvatar
        imgAvatar.setOnClickListener(v -> {
            if (isEditing) {
                showImagePickerDialog();
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            if (isEditing) {
                saveProfile();
            } else {
                enableEditing();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void requestPermissions() {
        String[] permissions = {
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        requestPermissions(permissions, 100);
    }

    private void loadUserInfo() {
        String token = sharedPreferences.getString("access_token", "");
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.getUserInfo("Bearer " + token);

        executorService.execute(() -> {
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserResponse userResponse = response.body();
                        runOnUiThread(() -> {
                            txtFullName.setText(userResponse.getFullname());
                            txtEmail.setText(userResponse.getUserDetails().getEmail());
                            txtPhone.setText(userResponse.getUserDetails().getPhone());
                            txtGender.setText(userResponse.getUserDetails().getGender() != null ? userResponse.getUserDetails().getGender() : "Not set");
                            txtDob.setText(userResponse.getUserDetails().getDateOfBirth() != null ? userResponse.getUserDetails().getDateOfBirth() : "Not set");

                            // Load ảnh đại diện
                            String profileImg = userResponse.getUserDetails().getProfileImg();
                            if (profileImg != null && !profileImg.isEmpty()) {
                                Glide.with(ProfileActivity.this)
                                        .load(profileImg)
                                        .circleCrop()
                                        .error(R.drawable.person) // Ảnh mặc định nếu tải thất bại
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                Log.e("ProfileActivity", "Failed to load profile image: " + e.getMessage());
                                                Toast.makeText(ProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                                                return false; // Cho phép Glide hiển thị ảnh mặc định
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                Log.d("ProfileActivity", "Profile image loaded successfully");
                                                return false;
                                            }
                                        })
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.person);
                            }

                            // Cập nhật lại SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("fullname", userResponse.getFullname());
                            editor.putString("email", userResponse.getUserDetails().getEmail());
                            editor.putString("phone", userResponse.getUserDetails().getPhone());
                            editor.putString("gender", userResponse.getUserDetails().getGender());
                            editor.putString("dateOfBirth", userResponse.getUserDetails().getDateOfBirth());
                            editor.putString("profile_img", userResponse.getUserDetails().getProfileImg());
                            editor.apply();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện", "Xóa ảnh đại diện"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            takePictureLauncher.launch(takePictureIntent);
                        }
                    } else if (which == 1) {
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickImageLauncher.launch(pickPhotoIntent);
                    } else if (which == 2) {
                        selectedImageUri = null;
                        imgAvatar.setImageResource(R.drawable.person);
                    }
                })
                .show();
    }

    private void showEditDialog(TextView textView, String hint) {
        if (!isEditing) return;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_text);
        EditText edtInput = dialog.findViewById(R.id.edt_input);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        edtInput.setHint(hint);
        edtInput.setText(textView.getText());

        btnSave.setOnClickListener(v -> {
            String newValue = edtInput.getText().toString().trim();
            if (!newValue.isEmpty()) {
                textView.setText(newValue);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showGenderDialog() {
        if (!isEditing) return;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_gender);
        RadioGroup rgGender = dialog.findViewById(R.id.rg_gender);
        RadioButton rbMale = dialog.findViewById(R.id.rb_male);
        RadioButton rbFemale = dialog.findViewById(R.id.rb_female);
        Button btnSaveGender = dialog.findViewById(R.id.btn_save_gender);

        if (txtGender.getText().toString().equals("Nam")) {
            rbMale.setChecked(true);
        } else if (txtGender.getText().toString().equals("Nữ")) {
            rbFemale.setChecked(true);
        }

        btnSaveGender.setOnClickListener(v -> {
            if (rgGender.getCheckedRadioButtonId() == R.id.rb_male) {
                txtGender.setText("Nam");
            } else if (rgGender.getCheckedRadioButtonId() == R.id.rb_female) {
                txtGender.setText("Nữ");
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker() {
        if (!isEditing) return;

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    txtDob.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void enableEditing() {
        isEditing = true;
        btnEditProfile.setText("Save Changes");
        btnEditName.setVisibility(View.VISIBLE);
        btnEditGender.setVisibility(View.VISIBLE);
        btnEditDob.setVisibility(View.VISIBLE);
        btnEditPhone.setVisibility(View.VISIBLE);
    }

    private String getMimeType(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            String fileExtension = getFileExtension(uri);
            switch (fileExtension.toLowerCase()) {
                case "jpg":
                case "jpeg":
                    mimeType = "image/jpeg";
                    break;
                case "png":
                    mimeType = "image/png";
                    break;
                default:
                    mimeType = "image/jpeg"; // Mặc định là JPEG nếu không xác định được
            }
        }
        return mimeType;
    }

    private String getFileExtension(Uri uri) {
        String path = getRealPathFromURI(uri);
        if (path != null) {
            return path.substring(path.lastIndexOf(".") + 1);
        }
        return "jpg"; // Mặc định là .jpg nếu không lấy được đuôi file
    }

    private String ensureFileExtension(String fileName, String defaultExtension) {
        if (fileName != null && !fileName.isEmpty()) {
            if (fileName.contains(".")) {
                return fileName;
            } else {
                return fileName + "." + defaultExtension;
            }
        }
        return "image." + defaultExtension;
    }

    private void saveProfile() {
        String name = txtFullName.getText().toString();
        String gender = txtGender.getText().toString().replaceAll("^\"|\"$", "");
        String dob = txtDob.getText().toString().trim();
        String phone = txtPhone.getText().toString();

        Log.d("ProfileActivity", "Raw dob before sending: " + dob); // Thêm log này

        String token = sharedPreferences.getString("access_token", "");
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        String dobToSend = dob.equals("Not set") || dob.isEmpty() ? null : dob;
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody genderBody = RequestBody.create(MediaType.parse("text/plain"), gender.equals("Not set") ? "" : gender);
        RequestBody dobBody = dobToSend == null ? null : RequestBody.create(MediaType.parse("text/plain"), dobToSend);

        Log.d("ProfileActivity", "dobToSend: " + dobToSend); // Thêm log này

        // Chuẩn bị file ảnh (nếu có)
        MultipartBody.Part profileImgPart = null;
        if (selectedImageUri != null) {
            String filePath = getRealPathFromURI(selectedImageUri);
            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    Log.d("ProfileActivity", "File path: " + file.getAbsolutePath());
                    String mimeType = getMimeType(selectedImageUri);
                    Log.d("ProfileActivity", "MIME type: " + mimeType);
                    String fileName = ensureFileExtension(file.getName(), getFileExtension(selectedImageUri));
                    Log.d("ProfileActivity", "File name: " + fileName);
                    RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), file);
                    profileImgPart = MultipartBody.Part.createFormData("profile_img", fileName, fileBody);
                } else {
                    Log.e("ProfileActivity", "File does not exist: " + filePath);
                    Toast.makeText(this, "Selected image file does not exist", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Log.e("ProfileActivity", "Failed to get file path from URI: " + selectedImageUri);
                Toast.makeText(this, "Failed to get file path for the selected image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Gọi API để cập nhật thông tin
        Call<UserResponse> call = apiService.updateProfile(
                "Bearer " + token,
                name,
                phone,
                gender.equals("Not set") ? "" : gender,
                dobToSend,
                profileImgPart
        );

        executorService.execute(() -> {
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            isEditing = false;
                            btnEditProfile.setText("Edit Information");
                            btnEditName.setVisibility(View.GONE);
                            btnEditGender.setVisibility(View.GONE);
                            btnEditDob.setVisibility(View.GONE);
                            btnEditPhone.setVisibility(View.GONE);

                            // Cập nhật lại SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("fullname", name);
                            editor.putString("phone", phone);
                            editor.putString("gender", gender.equals("Not set") ? null : gender);
                            editor.putString("dateOfBirth", dobToSend);
                            if (response.body().getUserDetails() != null) {
                                editor.putString("profile_img", response.body().getUserDetails().getProfileImg());
                            }
                            editor.apply();

                            // Đặt lại selectedImageUri sau khi lưu
                            selectedImageUri = null;

                            // Kết thúc activity và trả về kết quả
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Failed to update profile: " + response.message(), Toast.LENGTH_SHORT).show();
                            Log.e("ProfileActivity", "Response code: " + response.code());
                            if (response.errorBody() != null) {
                                try {
                                    Log.e("ProfileActivity", "Error body: " + response.errorBody().string());
                                } catch (Exception e) {
                                    Log.e("ProfileActivity", "Error reading error body: " + e.getMessage());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "API call failed: " + t.getMessage());
                    });
                }
            });
        });
    }

    // Hàm lấy đường dẫn thực tế từ URI
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }
}