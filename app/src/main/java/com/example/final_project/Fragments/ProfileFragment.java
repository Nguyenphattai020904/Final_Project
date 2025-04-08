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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.final_project.AddressManagementActivity;
import com.example.final_project.FeedbackActivity;
import com.example.final_project.Log.LogInActivity;
import com.example.final_project.OrderHistoryActivity;
import com.example.final_project.ProfileActivity;
import com.example.final_project.R;

public class ProfileFragment extends Fragment {
    private TextView txtFullName;
    private ImageView imgAvatar;
    private Button btnLogout;
    private LinearLayout btnShowInfo, btnPurchaseHistory, btnShippingAddress, btnFeedback, btnVouchers;
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
                        updateFullNameAndAvatar();
                    }
                }
        );

        sharedPreferences = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        txtFullName = view.findViewById(R.id.txt_full_name);
        imgAvatar = view.findViewById(R.id.img_avatar);
        updateFullNameAndAvatar();

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

        btnFeedback = view.findViewById(R.id.btn_feedback);
        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            startActivity(intent);
        });

        btnVouchers = view.findViewById(R.id.btn_vouchers);
        btnVouchers.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new VoucherFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogout = view.findViewById(R.id.btn_log_out);
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void updateFullNameAndAvatar() {
        String fullName = sharedPreferences.getString("fullname", "User");
        String profileImg = sharedPreferences.getString("profile_img", null);
        Log.d("ProfileFragment", "Retrieved Fullname: " + fullName);
        txtFullName.setText("Xin chào, " + fullName);

        // Load ảnh đại diện
        if (profileImg != null && !profileImg.isEmpty()) {
            Glide.with(this)
                    .load(profileImg)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.person);
        }
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