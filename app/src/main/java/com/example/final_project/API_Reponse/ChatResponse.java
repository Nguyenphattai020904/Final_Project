package com.example.final_project.API_Reponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatResponse {
    private boolean success;
    private String message;
    private String userPrompt;
    private String answer;

    @SerializedName("mentionedProducts")
    private List<MentionedProduct> mentionedProducts;

    @SerializedName("retryAfter")
    private Integer retryAfter;

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

    public List<MentionedProduct> getMentionedProducts() {
        return mentionedProducts;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }
}