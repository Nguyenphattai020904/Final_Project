package com.example.final_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.NotificationResponse;
import com.example.final_project.Notification; // Import đúng lớp Notification
import com.example.final_project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList, this::deleteNotification);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        String token = getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("access_token", "");
        int userId = Integer.parseInt(getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("userId", "0"));
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<NotificationResponse> call = apiService.getNotifications(userId, "Bearer " + token);
        call.enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    List<Notification> fetchedNotifications = response.body().getNotifications(); // Sử dụng đúng kiểu
                    if (fetchedNotifications != null) {
                        notificationList.addAll(fetchedNotifications); // Thêm danh sách thông báo từ API
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Danh sách thông báo rỗng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải thông báo: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteNotification(int id) {
        String token = getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("access_token", "");
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<Void> call = apiService.deleteNotification(id, "Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    notificationList.removeIf(n -> n.getId() == id);
                    adapter.notifyDataSetChanged();
                    updateNotificationBadge();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationBadge() {
        Fragment homeFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (homeFragment instanceof HomeFragment) {
            ((HomeFragment) homeFragment).updateNotificationBadge();
        }
    }
}

class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications;
    private final OnDeleteClickListener deleteClickListener;

    public NotificationAdapter(List<Notification> notifications, OnDeleteClickListener deleteClickListener) {
        this.notifications = notifications;
        this.deleteClickListener = deleteClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false); // Sửa tên layout thành notification_item
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Hiển thị nội dung thông báo
        holder.messageTextView.setText(notification.getMessage());

        // Định dạng và hiển thị timestamp
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = sdf.parse(notification.getCreatedAt());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = outputFormat.format(date);
            holder.timestampTextView.setText(formattedDate);
        } catch (Exception e) {
            holder.timestampTextView.setText(notification.getCreatedAt()); // Hiển thị nguyên bản nếu parse lỗi
        }

        // Xử lý sự kiện xóa
        holder.deleteButton.setOnClickListener(v -> deleteClickListener.onDeleteClick(notification.getId()));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_notification_message);
            timestampTextView = itemView.findViewById(R.id.tv_notification_timestamp);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    interface OnDeleteClickListener {
        void onDeleteClick(int id);
    }
}