package com.example.final_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.Activity.MainActivity;
import com.example.final_project.Adapter.CartAdapter;
import com.example.final_project.Products.Product;
import com.example.final_project.R;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnQuantityChangeListener {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private LinearLayout emptyCartContainer; // Container cho icon và message
    private Button buyNowButton;
    private CheckBox selectAllCheckBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cart_recycler_view);
        emptyCartContainer = view.findViewById(R.id.empty_cart_container); // Khởi tạo container
        buyNowButton = view.findViewById(R.id.btnBuyNow);
        selectAllCheckBox = view.findViewById(R.id.checkbox_select_all);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(getContext(), CartManager.getInstance(getContext()).getCartItems(), this);
        recyclerView.setAdapter(cartAdapter);

        int spacingInPx = Math.round(5 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new CartAdapter.ItemSpacingDecoration(spacingInPx));

        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartAdapter.selectAll(isChecked);
            cartAdapter.notifyDataSetChanged();
        });

        updateCartUI();

        buyNowButton.setOnClickListener(v -> {
            List<Product> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            } else {
                // Tạo PaymentFragment và truyền selectedItems qua Bundle
                PaymentFragment paymentFragment = new PaymentFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedItems", new ArrayList<>(selectedItems));
                paymentFragment.setArguments(bundle);

                // Chuyển sang PaymentFragment
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, paymentFragment)
                        .addToBackStack(null)
                        .commit();

                // Xóa các sản phẩm đã chọn khỏi giỏ hàng
                for (Product item : selectedItems) {
                    CartManager.getInstance(getContext()).removeItem(item);
                }
                cartAdapter.notifyDataSetChanged();
                updateCartUI();
            }
        });

        return view;
    }

    public void addToCart(Product product) {
        CartManager.getInstance(getContext()).addToCart(product);
        cartAdapter.notifyDataSetChanged();
        updateCartUI();
    }

    public void updateCartFromManager() {
        cartAdapter.notifyDataSetChanged();
        updateCartUI();
    }

    private void updateCartUI() {
        List<Product> cartItems = CartManager.getInstance(getContext()).getCartItems();
        if (cartItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyCartContainer.setVisibility(View.VISIBLE); // Hiển thị container khi giỏ hàng trống
            buyNowButton.setVisibility(View.GONE);
            selectAllCheckBox.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyCartContainer.setVisibility(View.GONE); // Ẩn container khi giỏ hàng có sản phẩm
            buyNowButton.setVisibility(View.VISIBLE);
            selectAllCheckBox.setVisibility(View.VISIBLE);
        }
        cartAdapter.notifyDataSetChanged();
        updateCartBadge();

        updateSelectAllCheckBox();
    }

    @Override
    public void onQuantityChanged() {
        updateCartUI();
    }

    @Override
    public void onSelectionChanged() {
        updateSelectAllCheckBox();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartUI();
    }

    private void updateCartBadge() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.updateCartBadge(CartManager.getInstance(getContext()).getCartSize());
        }
    }

    public List<Product> getSelectedItems() {
        return cartAdapter != null ? cartAdapter.getSelectedItems() : new ArrayList<>();
    }

    public void updateSelectAllCheckBox() {
        List<Product> cartItems = CartManager.getInstance(getContext()).getCartItems();
        List<Product> selectedItems = cartAdapter.getSelectedItems();
        if (cartItems.isEmpty()) {
            selectAllCheckBox.setChecked(false);
        } else {
            selectAllCheckBox.setChecked(selectedItems.size() == cartItems.size());
        }
    }
}