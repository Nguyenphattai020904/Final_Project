package com.example.final_project;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeminiResponse {
    @SerializedName("candidates")
    private List<Candidate> candidates;

    @SerializedName("promptFeedback")
    private PromptFeedback promptFeedback;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public static class Candidate {
        @SerializedName("content")
        private Content content;

        @SerializedName("finishReason")
        private String finishReason;

        @SerializedName("index")
        private int index;

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        @SerializedName("parts")
        private List<Part> parts;

        @SerializedName("role")
        private String role;

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public String getText() {
            return text;
        }
    }

    public static class PromptFeedback {
        @SerializedName("blockReason")
        private String blockReason;

        @SerializedName("safetyRatings")
        private List<SafetyRating> safetyRatings;
    }

    public static class SafetyRating {
        @SerializedName("category")
        private String category;

        @SerializedName("probability")
        private String probability;
    }

    // Phương thức tiện ích để lấy nhanh text response
    public String getResponse() {
        try {
            if (candidates != null && !candidates.isEmpty() &&
                    candidates.get(0).content != null &&
                    candidates.get(0).content.parts != null &&
                    !candidates.get(0).content.parts.isEmpty()) {
                return candidates.get(0).content.parts.get(0).getText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Không nhận được phản hồi từ AI";
    }
}