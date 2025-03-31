package com.example.final_project.API_Requests;

public class FeedbackRequest {
    private int user_id;
    private String name;
    private String feedback;

    public FeedbackRequest(int user_id, String name, String feedback) {
        this.user_id = user_id;
        this.name = name;
        this.feedback = feedback;
    }

    // Getters (cần thiết để Retrofit serialize thành JSON)
    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getFeedback() {
        return feedback;
    }
}