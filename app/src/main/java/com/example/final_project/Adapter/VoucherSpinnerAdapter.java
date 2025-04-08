package com.example.final_project.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.final_project.R;
import com.example.final_project.Voucher;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherSpinnerAdapter extends ArrayAdapter<Voucher> {
    private final List<Voucher> vouchers;
    private final double totalCost;

    public VoucherSpinnerAdapter(Context context, List<Voucher> vouchers, double totalCost) {
        super(context, 0, vouchers);
        this.vouchers = vouchers;
        this.totalCost = totalCost;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.voucher_spinner_item, parent, false);
        }

        Voucher voucher = vouchers.get(position);

        ImageView voucherImage = convertView.findViewById(R.id.voucher_image);
        TextView voucherName = convertView.findViewById(R.id.voucher_name);
        TextView voucherExpiry = convertView.findViewById(R.id.voucher_expiry);

        // Xử lý ảnh voucher
        String imageUrl = voucher.getVoucherImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.img) // Ảnh mặc định khi đang tải
                    .error(R.drawable.img) // Ảnh mặc định nếu lỗi
                    .into(voucherImage);
        } else {
            voucherImage.setImageResource(R.drawable.img); // Ảnh mặc định nếu URL rỗng
        }

        // Hiển thị tên voucher
        voucherName.setText(voucher.getVoucherName());

        // Hiển thị ngày hết hạn
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String expiryDate = voucher.getVoucherDate() != null ? sdf.format(voucher.getVoucherDate()) : "Không xác định";
        voucherExpiry.setText("Hết hạn: " + expiryDate);

        return convertView;
    }
}