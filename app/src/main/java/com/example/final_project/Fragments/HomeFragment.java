package com.example.final_project.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.ProductResponse;
import com.example.final_project.API_Reponse.UnreadCountResponse;
import com.example.final_project.Products.CarouselAdapter;
import com.example.final_project.Products.Product;
import com.example.final_project.Products.ProductAdapter;
import com.example.final_project.Products.ProductDetailFragment;
import com.example.final_project.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView, bestSellerRecyclerView;
    private ProductAdapter productAdapter, bestSellerAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> fullProductList = new ArrayList<>();
    private List<Product> bestSellerList = new ArrayList<>();
    private List<Integer> bestSellerIds = new ArrayList<>();
    private ViewPager2 carouselViewPager;
    private CarouselAdapter carouselAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;
    private EditText searchEditText;
    private ImageButton searchButton, menuButton, btnNotification;
    private TextView tvNotificationBadge;
    private PopupWindow popupWindow;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // RecyclerView cho tất cả sản phẩm
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList, this, bestSellerIds);
        recyclerView.setAdapter(productAdapter);

        int spacingInDp = 1;
        int spacingInPx = Math.round(spacingInDp * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacingInPx));

        // RecyclerView cho Best Seller
        bestSellerRecyclerView = view.findViewById(R.id.best_seller_recycler_view);
        bestSellerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bestSellerAdapter = new ProductAdapter(getContext(), bestSellerList, this, bestSellerIds);
        bestSellerRecyclerView.setAdapter(bestSellerAdapter);

        carouselViewPager = view.findViewById(R.id.carousel_view_pager);
        carouselAdapter = new CarouselAdapter(getContext());
        carouselViewPager.setAdapter(carouselAdapter);

        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        menuButton = view.findViewById(R.id.menu_button);
        btnNotification = view.findViewById(R.id.btnNotification);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);

        setupSearchListener();
        setupMenuButton();
        setupNotificationButton();

        // Tải dữ liệu theo thứ tự: Best Seller -> Products
        loadData();

        setupCarousel();

        return view;
    }

    private void loadData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Bước 1: Lấy danh sách Best Seller trước
        Call<ProductResponse> bestSellerCall = apiService.getBestSellers();
        bestSellerCall.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bestSellerList.clear();
                    bestSellerList.addAll(response.body().getBestSellers());
                    bestSellerIds.clear();
                    bestSellerIds.addAll(bestSellerList.stream().map(Product::getProductId).collect(Collectors.toList()));
                    Log.d("HomeFragment", "BestSellerIds loaded: " + bestSellerIds);

                    // Bước 2: Sau khi có Best Seller, lấy danh sách sản phẩm
                    fetchProducts();
                } else {
                    Toast.makeText(getContext(), "Không tải được Best Seller: " + response.code(), Toast.LENGTH_LONG).show();
                    fetchProducts(); // Vẫn tải sản phẩm nếu Best Seller lỗi
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải Best Seller: " + t.getMessage(), Toast.LENGTH_LONG).show();
                fetchProducts(); // Vẫn tải sản phẩm nếu lỗi
            }
        });
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
                    bestSellerAdapter.notifyDataSetChanged();
                    Log.d("HomeFragment", "Products loaded, notifying adapters");
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

    private void setupNotificationButton() {
        btnNotification.setOnClickListener(v -> {
            NotificationFragment notificationFragment = new NotificationFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, notificationFragment)
                    .addToBackStack(null)
                    .commit();
            clearNotificationBadge();
        });
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
                filterProducts(category, button);
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
        productList.clear();

        if (query.isEmpty()) {
            productList.addAll(fullProductList);
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
            Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterProducts(String mainCategory, Button clickedButton) {
        productList.clear();
        if (mainCategory.equals("Tất Cả")) {
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

    private void setupCarousel() {
        if (carouselRunnable != null) {
            handler.removeCallbacks(carouselRunnable);
        }
        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = carouselViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % 5;
                carouselViewPager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(carouselRunnable, 3000);
    }

    public void updateNotificationBadge() {
        if (getActivity() == null) return;

        String token = getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("access_token", "");
        int userId = Integer.parseInt(getActivity().getSharedPreferences("userPrefs", getContext().MODE_PRIVATE).getString("userId", "0"));
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<UnreadCountResponse> call = apiService.getUnreadCount(userId, "Bearer " + token);
        call.enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().getUnreadCount();
                    if (count > 0) {
                        tvNotificationBadge.setText(String.valueOf(count));
                        tvNotificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvNotificationBadge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                Log.e("HomeFragment", "Failed to update badge: " + t.getMessage());
            }
        });
    }

    private void clearNotificationBadge() {
        updateNotificationBadge();
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
    @Override
    public void onStart() {
        super.onStart();
        updateNotificationBadge(); // Cập nhật badge mỗi khi fragment được hiển thị
    }
}