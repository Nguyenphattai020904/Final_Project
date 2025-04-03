package com.example.final_project.API_Reponse;

import com.google.gson.annotations.SerializedName;

public class OrderResponse {
    private boolean success;
    private String message;
    private int pendingOrderId;

    @SerializedName("zaloPayUrl")
    private String zaloPayUrl;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getPendingOrderId() {
        return pendingOrderId;
    }

    public String getZaloPayUrl() {
        return zaloPayUrl;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", pendingOrderId=" + pendingOrderId +
                ", zaloPayUrl='" + zaloPayUrl + '\'' +
                '}';
    }
}