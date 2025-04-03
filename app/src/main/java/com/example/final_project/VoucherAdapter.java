package com.example.final_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

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
        holder.voucherDate.setText("Hết hạn: " + voucher.getVoucherDate());
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