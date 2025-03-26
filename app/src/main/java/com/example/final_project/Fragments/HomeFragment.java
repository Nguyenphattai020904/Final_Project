package com.example.final_project.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.ProductResponse;
import com.example.final_project.Products.CarouselAdapter;
import com.example.final_project.Products.Product;
import com.example.final_project.Products.ProductAdapter;
import com.example.final_project.Products.ProductDetailFragment;
import com.example.final_project.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener, CarouselAdapter.OnCarouselItemClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> fullProductList = new ArrayList<>();
    private Button btnFreshFood, btnDrinks, btnDryFood, btnSpice;
    private Button selectedButton = null;
    private ViewPager2 carouselViewPager;
    private CarouselAdapter carouselAdapter;
    private List<Product> topProducts = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;
    private EditText searchEditText;
    private ImageButton searchButton;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        productAdapter = new ProductAdapter(getContext(), productList, this);
        recyclerView.setAdapter(productAdapter);

        int spacingInDp = 1;
        int spacingInPx = Math.round(spacingInDp * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacingInPx));

        // Khởi tạo các button
        btnFreshFood = view.findViewById(R.id.btn_fresh_food);
        btnDrinks = view.findViewById(R.id.btn_drinks);
        btnDryFood = view.findViewById(R.id.btn_dry_food);
        btnSpice = view.findViewById(R.id.btn_spice);
        setupButtonListeners();

        // Khởi tạo ViewPager2 cho carousel
        carouselViewPager = view.findViewById(R.id.carousel_view_pager);
        carouselAdapter = new CarouselAdapter(getContext(), topProducts, this);
        carouselViewPager.setAdapter(carouselAdapter);

        // Khởi tạo thanh tìm kiếm
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        setupSearchListener();

        // Lấy dữ liệu từ API
        fetchProducts();

        return view;
    }

    private void setupButtonListeners() {
        btnFreshFood.setOnClickListener(v -> filterProducts("Fresh Food", btnFreshFood));
        btnDrinks.setOnClickListener(v -> filterProducts("Drinks", btnDrinks));
        btnDryFood.setOnClickListener(v -> filterProducts("Dry Food", btnDryFood));
        btnSpice.setOnClickListener(v -> filterProducts("Spices", btnSpice));
    }

    private void setupSearchListener() {
        searchButton.setOnClickListener(v -> performSearch());

        // Tìm kiếm khi nhấn Enter trên bàn phím
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

    private void performSearch() {
        String query = searchEditText.getText().toString().trim().toLowerCase();
        productList.clear();

        if (query.isEmpty()) {
            productList.addAll(fullProductList); // Hiển thị tất cả nếu không có từ khóa
        } else {
            for (Product product : fullProductList) {
                if ((product.getName() != null && product.getName().toLowerCase().contains(query)) ||
                        (product.getCategory() != null && product.getCategory().toLowerCase().contains(query)) ||
                        (product.getMainCategory() != null && product.getMainCategory().toLowerCase().contains(query))) {
                    productList.add(product);
                }
            }
        }

        productAdapter.notifyDataSetChanged();
        if (productList.isEmpty()) {
            Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterProducts(String mainCategory, Button clickedButton) {
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.green_smoke)));
        }
        clickedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        selectedButton = clickedButton;

        productList.clear();
        if (mainCategory.equals("All")) {
            productList.addAll(fullProductList);
        } else {
            for (Product product : fullProductList) {
                if (product.getMainCategory() != null && product.getMainCategory().equals(mainCategory)) {
                    productList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void fetchProducts() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getProducts();
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullProductList.clear();
                    fullProductList.addAll(response.body().getProducts());
                    productList.clear();
                    productList.addAll(fullProductList);
                    productAdapter.notifyDataSetChanged();

                    // Lấy 10 sản phẩm có số lượng cao nhất
                    setupCarousel();
                } else {
                    Toast.makeText(getContext(), "Failed to load products: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCarousel() {
        topProducts.clear();
        List<Product> sortedList = new ArrayList<>(fullProductList);
        Collections.sort(sortedList, (p1, p2) -> Integer.compare(p2.getQuantity(), p1.getQuantity()));
        topProducts.addAll(sortedList.subList(0, Math.min(10, sortedList.size())));
        carouselAdapter.notifyDataSetChanged();

        // Tự động chuyển slide mỗi 3 giây
        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = carouselViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % topProducts.size();
                carouselViewPager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(carouselRunnable, 3000);
    }

    @Override
    public void onProductClick(Product product) {
        navigateToDetail(product);
    }

    @Override
    public void onCarouselItemClick(Product product) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(carouselRunnable);
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