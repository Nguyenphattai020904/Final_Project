package com.example.final_project.API_Reponse;

import com.example.final_project.Notification; // Import lớp Notification của bạn

import java.util.List;

public class NotificationResponse {
    private boolean success;
    private List<Notification> notifications; // Sử dụng com.example.final_project.Notification

    public boolean isSuccess() {
        return success;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}