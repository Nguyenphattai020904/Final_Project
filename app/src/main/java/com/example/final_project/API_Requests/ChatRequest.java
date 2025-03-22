package com.example.final_project.API_Requests;

public class ChatRequest {
    private String userId;
    private String userPrompt; // Changed from "message" to "userPrompt"

    public ChatRequest(String userId, String userPrompt) {
        this.userId = userId;
        this.userPrompt = userPrompt; // Updated parameter name
    }
}