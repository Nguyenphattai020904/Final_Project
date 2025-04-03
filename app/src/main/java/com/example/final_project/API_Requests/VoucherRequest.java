package com.example.final_project.API_Requests;

public class VoucherRequest {
    private int voucher_id;
    private double order_total;

    public VoucherRequest(int voucher_id, double order_total) {
        this.voucher_id = voucher_id;
        this.order_total = order_total;
    }
}