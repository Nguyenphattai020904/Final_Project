package com.example.final_project;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Voucher {
    @SerializedName("voucher_id")
    private int voucherId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("voucher_name")
    private String voucherName;

    @SerializedName("voucher_value")
    private double voucherValue;

    @SerializedName("voucher_type")
    private String voucherType;

    @SerializedName("voucher_quantity")
    private int voucherQuantity;

    @SerializedName("voucher_date")
    private Date voucherDate;

    @SerializedName("min_order_value")
    private double minOrderValue;

    @SerializedName("voucher_image")
    private String voucherImage;

    // Constructor đầy đủ
    public Voucher(int voucherId, int userId, String voucherName, double voucherValue, String voucherType,
                   int voucherQuantity, Date voucherDate, double minOrderValue, String voucherImage) {
        this.voucherId = voucherId;
        this.userId = userId;
        this.voucherName = voucherName;
        this.voucherValue = voucherValue;
        this.voucherType = voucherType;
        this.voucherQuantity = voucherQuantity;
        this.voucherDate = voucherDate;
        this.minOrderValue = minOrderValue;
        this.voucherImage = voucherImage;
    }

    // Getter
    public int getVoucherId() {
        return voucherId;
    }

    public String getVoucherName() {
        return voucherName != null ? voucherName : "Không xác định";
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public String getVoucherImage() {
        return voucherImage;
    }

    public double getVoucherValue() {
        return voucherValue;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public boolean isApplicable(double totalCost) {
        return totalCost >= minOrderValue;
    }
}