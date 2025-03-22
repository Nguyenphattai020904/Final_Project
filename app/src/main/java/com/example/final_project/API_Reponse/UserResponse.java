package com.example.final_project.API_Reponse;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    private String message;
    private String token;
    private boolean success;

    @SerializedName("name") // Map the JSON field 'name' to the 'fullname' field in Java
    private String fullname;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
