package com.example.final_project;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // Số cột là 3

        // Tạo dữ liệu sản phẩm giả
        List<Product> productList = ProductData.getFakeProducts(10); // Số lượng sản phẩm giả

        // Khởi tạo adapter và gán vào RecyclerView
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);
        int spacingInDp = 1; // Khoảng cách 1dp
        int spacingInPx = Math.round(spacingInDp * getResources().getDisplayMetrics().density);

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacingInPx));


        return view;
    }
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int spacing; // Khoảng cách tính bằng pixel

        public GridSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;

            // Thêm khoảng cách phía trên cho hàng đầu tiên
            if (parent.getChildAdapterPosition(view) < 3) { // Giả sử số cột là 3
                outRect.top = spacing;
            }
        }
    }
}


