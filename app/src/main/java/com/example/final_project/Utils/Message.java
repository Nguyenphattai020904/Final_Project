package com.example.final_project.Utils;

// Message.java
public class Message {
    private String text;
    private boolean isUser;

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}
