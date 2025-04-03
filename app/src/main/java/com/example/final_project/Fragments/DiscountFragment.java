package com.example.final_project.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.ProductResponse;
import com.example.final_project.Products.Product;
import com.example.final_project.Products.ProductAdapter;
import com.example.final_project.Products.ProductDetailFragment;
import com.example.final_project.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscountFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> discountProductList = new ArrayList<>();
    private List<Product> fullDiscountList = new ArrayList<>();
    private EditText searchEditText;
    private ImageButton searchButton, menuButton;
    private PopupWindow popupWindow;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discount, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_discount);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), discountProductList, this);
        recyclerView.setAdapter(productAdapter);

        int spacingInDp = 1;
        int spacingInPx = Math.round(spacingInDp * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacingInPx));

        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        menuButton = view.findViewById(R.id.menu_button);
        setupSearchListener();
        setupMenuButton();

        fetchDiscountProducts();

        return view;
    }

    private void setupSearchListener() {
        searchButton.setOnClickListener(v -> performSearch());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void setupMenuButton() {
        menuButton.setOnClickListener(v -> showCategoryMenu(v));
    }

    private void showCategoryMenu(View anchorView) {
        LinearLayout menuLayout = new LinearLayout(getContext());
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.WHITE);
        menuLayout.setPadding(16, 16, 16, 16);

        String[] categories = {"Tất Cả", "Thực Phẩm Tươi", "Đồ Uống", "Thực Phẩm Khô", "Gia Vị"};
        for (String category : categories) {
            Button button = new Button(getContext());
            button.setText(category);
            button.setTextSize(14);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.green_smoke)));
            button.setTextColor(getResources().getColor(R.color.yellow));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            button.setLayoutParams(params);
            button.setOnClickListener(v -> {
                filterProducts(category);
                popupWindow.dismiss();
            });
            menuLayout.addView(button);
        }

        popupWindow = new PopupWindow(menuLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(8f);
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.START);
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim().toLowerCase();
        discountProductList.clear();

        if (query.isEmpty()) {
            discountProductList.addAll(fullDiscountList);
        } else {
            for (Product product : fullDiscountList) {
                if ((product.getName() != null && product.getName().toLowerCase().contains(query)) ||
                        (product.getCategory() != null && product.getCategory().toLowerCase().contains(query)) ||
                        (product.getMainCategory() != null && product.getMainCategory().toLowerCase().contains(query))) {
                    discountProductList.add(product);
                }
            }
        }

        productAdapter.notifyDataSetChanged();
        if (discountProductList.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy sản phẩm giảm giá", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterProducts(String mainCategory) {
        discountProductList.clear();
        if (mainCategory.equals("Tất Cả")) {
            discountProductList.addAll(fullDiscountList);
        } else {
            for (Product product : fullDiscountList) {
                if (product.getMainCategory() != null && product.getMainCategory().equals(mainCategory)) {
                    discountProductList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void fetchDiscountProducts() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getProducts();
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullDiscountList.clear();
                    List<Product> allProducts = response.body().getProducts();
                    for (Product product : allProducts) {
                        if (product.getDiscount() != null && product.getDiscount() > 0) {
                            fullDiscountList.add(product);
                        }
                    }
                    discountProductList.clear();
                    discountProductList.addAll(fullDiscountList);
                    productAdapter.notifyDataSetChanged();
                    if (discountProductList.isEmpty()) {
                        Toast.makeText(getContext(), "Không có sản phẩm giảm giá", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Không tải được sản phẩm: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        navigateToDetail(product);
    }

    private void navigateToDetail(Product product) {
        ProductDetailFragment detailFragment = new ProductDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product);
        detailFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public GridSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;
            if (parent.getChildAdapterPosition(view) < 3) {
                outRect.top = spacing;
            }
        }
    }
}