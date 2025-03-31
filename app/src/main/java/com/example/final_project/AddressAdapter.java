package com.example.final_project;

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
    private final OnAddressActionListener editListener;
    private final OnAddressActionListener deleteListener;

    public interface OnAddressActionListener {
        void onAddressAction(Address address);
    }

    public AddressAdapter(List<Address> addressList, OnAddressActionListener editListener, OnAddressActionListener deleteListener) {
        this.addressList = addressList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
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
        holder.btnEdit.setOnClickListener(v -> editListener.onAddressAction(address));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onAddressAction(address));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView txtAddress;
        Button btnEdit, btnDelete;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAddress = itemView.findViewById(R.id.txt_address);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}