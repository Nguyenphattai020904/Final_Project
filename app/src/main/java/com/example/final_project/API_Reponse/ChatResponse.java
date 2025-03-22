package com.example.final_project.API_Reponse;

public class ChatResponse {
    private boolean success;
    private String message;
    private String userPrompt;
    private String answer;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        // If answer exists, return that, otherwise return the message (which might be an error)
        return answer != null && !answer.isEmpty() ? answer : message;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public String getAnswer() {
        return answer;
    }
}