package com.example.final_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.MainActivity;
import com.example.final_project.Products.Product;
import com.example.final_project.R;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnQuantityChangeListener {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter; // Vẫn giữ private
    private TextView emptyCartMessage;
    private Button buyNowButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cart_recycler_view);
        emptyCartMessage = view.findViewById(R.id.empty_cart_message);
        buyNowButton = view.findViewById(R.id.btnBuyNow);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(getContext(), CartManager.getInstance().getCartItems(), this);
        recyclerView.setAdapter(cartAdapter);

        int spacingInPx = Math.round(5 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new CartAdapter.ItemSpacingDecoration(spacingInPx));

        updateCartUI();

        buyNowButton.setOnClickListener(v -> {
            List<Product> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            } else {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new PaymentFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    public void addToCart(Product product) {
        CartManager.getInstance().addToCart(product);
        cartAdapter.notifyDataSetChanged();
        updateCartUI();
    }

    public void updateCartFromManager() {
        cartAdapter.notifyDataSetChanged();
        updateCartUI();
    }

    private void updateCartUI() {
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyCartMessage.setVisibility(View.VISIBLE);
            buyNowButton.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyCartMessage.setVisibility(View.GONE);
            buyNowButton.setVisibility(View.VISIBLE);
        }
        updateCartBadge();
    }

    @Override
    public void onQuantityChanged() {
        updateCartUI();
    }

    private void updateCartBadge() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.updateCartBadge(CartManager.getInstance().getCartSize());
        }
    }

    // Thêm phương thức công khai để lấy danh sách sản phẩm được chọn
    public List<Product> getSelectedItems() {
        return cartAdapter != null ? cartAdapter.getSelectedItems() : new ArrayList<>();
    }
}