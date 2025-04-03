package com.example.final_project;

public class Notification {
    private int id;
    private String type;
    private String message;
    private boolean isRead;
    private String createdAt;
    private Integer relatedId;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Integer getRelatedId() {
        return relatedId;
    }
}