package com.example.final_project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private TextView txtFullName;
    private Button btnLogout;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Khởi tạo SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        // Hiển thị tên đầy đủ người dùng
        String fullName = sharedPreferences.getString("fullName", "User");
        txtFullName = view.findViewById(R.id.txt_full_name);
        txtFullName.setText(fullName);

        // Xử lý sự kiện đăng xuất
        btnLogout = view.findViewById(R.id.btn_log_out);
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    // Hàm xử lý đăng xuất
    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putString("fullName", "");
        editor.apply();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(getActivity(), LogInActivity.class);
        startActivity(intent);
        getActivity().finish();
    }




}


