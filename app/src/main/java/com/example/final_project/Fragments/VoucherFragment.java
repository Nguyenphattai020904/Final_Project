package com.example.final_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.R;
import com.example.final_project.Voucher;
import com.example.final_project.VoucherAdapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherFragment extends Fragment {
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voucher, container, false);
        recyclerView = view.findViewById(R.id.voucher_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadVouchers();
        return view;
    }

    private void loadVouchers() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String userIdStr = getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("userId", "0");
        int userId = Integer.parseInt(userIdStr);

        Call<List<Voucher>> call = apiService.getVouchers(userId);
        call.enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VoucherAdapter adapter = new VoucherAdapter(getContext(), response.body());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải voucher: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}