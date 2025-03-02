package com.example.final_project;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    @SerializedName("contents")
    private List<Content> contents;

    @SerializedName("generationConfig")
    private GenerationConfig generationConfig;

    public GeminiRequest(String userMessage) {
        // Khởi tạo danh sách contents
        this.contents = new ArrayList<>();

        // Tạo phần content chứa message của user
        Content content = new Content();
        content.setRole("user");

        // Tạo phần part chứa text
        List<Part> parts = new ArrayList<>();
        Part part = new Part();
        part.setText(userMessage);
        parts.add(part);

        content.setParts(parts);
        this.contents.add(content);

        // Cấu hình generation
        this.generationConfig = new GenerationConfig();
    }

    public static class Content {
        @SerializedName("role")
        private String role;

        @SerializedName("parts")
        private List<Part> parts;

        public void setRole(String role) {
            this.role = role;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class GenerationConfig {
        @SerializedName("temperature")
        private double temperature = 0.7;

        @SerializedName("maxOutputTokens")
        private int maxOutputTokens = 800;

        @SerializedName("topP")
        private double topP = 0.95;

        @SerializedName("topK")
        private int topK = 40;
    }
}