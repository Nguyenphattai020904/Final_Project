package com.example.final_project.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.Voucher;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private List<Voucher> vouchers;
    private Context context;

    public VoucherAdapter(Context context, List<Voucher> vouchers) {
        this.context = context;
        this.vouchers = vouchers;
    }

    @Override
    public VoucherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.voucher_item, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);
        holder.voucherName.setText(voucher.getVoucherName());

        // Định dạng ngày hết hạn
        Date voucherDate = voucher.getVoucherDate();
        String formattedDate = "Không xác định"; // Giá trị mặc định nếu có lỗi

        if (voucherDate != null) {
            // Định dạng Date thành chuỗi yyyy/MM/dd
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            formattedDate = outputFormat.format(voucherDate);
        }

        holder.voucherDate.setText("Hết hạn: " + formattedDate);
        Picasso.get().load(voucher.getVoucherImage()).into(holder.voucherImage);
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ImageView voucherImage;
        TextView voucherName, voucherDate;

        public VoucherViewHolder(View itemView) {
            super(itemView);
            voucherImage = itemView.findViewById(R.id.voucher_image);
            voucherName = itemView.findViewById(R.id.voucher_name);
            voucherDate = itemView.findViewById(R.id.voucher_date);
        }
    }
}