package com.example.final_project.Products;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {
    private Context context;

    // Mảng chứa ID của 5 ảnh trong drawable
    private final int[] bannerImages = {
            R.drawable.banner_one,
            R.drawable.banner_two,
            R.drawable.banner_three,
            R.drawable.banner_four,
            R.drawable.banner_five
    };

    public CarouselAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.carousel_item, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        // Gán ảnh từ drawable dựa trên position (lặp lại nếu vượt quá 5)
        int imageIndex = position % bannerImages.length;

        // Giải mã và thu nhỏ ảnh
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // Giảm kích thước ảnh xuống 1/2 (có thể điều chỉnh)
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), bannerImages[imageIndex], options);
        holder.productImage.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return bannerImages.length; // Chỉ trả về 5 item
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.carousel_image);
            // Ẩn TextView nếu không cần
            View productName = itemView.findViewById(R.id.carousel_product_name);
            if (productName != null) {
                productName.setVisibility(View.GONE);
            }
        }
    }
}