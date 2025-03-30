package com.example.final_project;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<Address> addressList;
    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public AddressAdapter(List<Address> addressList, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.addressList = addressList;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.txtAddress.setText(address.toString());
        holder.btnEdit.setOnClickListener(v -> editClickListener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDelete(address));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView txtAddress;
        Button btnEdit, btnDelete;

        @SuppressLint("WrongViewCast")
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAddress = itemView.findViewById(R.id.txt_address);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    interface OnEditClickListener {
        void onEdit(Address address);
    }

    interface OnDeleteClickListener {
        void onDelete(Address address);
    }
}