package com.example.final_project.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.final_project.AddressManagementActivity;
import com.example.final_project.FeedbackActivity; // Thêm import
import com.example.final_project.Log.LogInActivity;
import com.example.final_project.OrderHistoryActivity;
import com.example.final_project.ProfileActivity;
import com.example.final_project.R;

public class ProfileFragment extends Fragment {
    private TextView txtFullName;
    private Button btnLogout;
    private LinearLayout btnShowInfo, btnPurchaseHistory, btnShippingAddress, btnFeedback; // Thêm btnFeedback
    private SharedPreferences sharedPreferences;

    private ActivityResultLauncher<Intent> profileActivityLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        updateFullName();
                    }
                }
        );

        sharedPreferences = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        txtFullName = view.findViewById(R.id.txt_full_name);
        updateFullName();

        btnShowInfo = view.findViewById(R.id.btn_show_info);
        btnShowInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            profileActivityLauncher.launch(intent);
        });

        btnPurchaseHistory = view.findViewById(R.id.btn_purchase_history);
        btnPurchaseHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnShippingAddress = view.findViewById(R.id.btn_shipping_address);
        btnShippingAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddressManagementActivity.class);
            startActivity(intent);
        });

        // Thêm sự kiện cho Feedback / Contact
        btnFeedback = view.findViewById(R.id.btn_feedback);
        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            startActivity(intent);
        });

        btnLogout = view.findViewById(R.id.btn_log_out);
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void updateFullName() {
        String fullName = sharedPreferences.getString("fullname", "User");
        Log.d("ProfileFragment", "Retrieved Fullname: " + fullName);
        txtFullName.setText("Hello, " + fullName);
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), LogInActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}