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
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.final_project.Log.LogInActivity;
import com.example.final_project.R;

public class ProfileFragment extends Fragment {
    private TextView txtFullName;
    private Button btnLogout;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Retrieve fullname from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String fullName = sharedPreferences.getString("fullname", "User"); // Fallback to "User" if null
        Log.d("ProfileFragment", "Retrieved Fullname: " + fullName);

        // Display fullname
        txtFullName = view.findViewById(R.id.txt_full_name);
        txtFullName.setText(fullName);

        // Logout button handling
        btnLogout = view.findViewById(R.id.btn_log_out);
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }


    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all user-related data
        editor.apply();

        // Navigate back to the login screen
        Intent intent = new Intent(getActivity(), LogInActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}


