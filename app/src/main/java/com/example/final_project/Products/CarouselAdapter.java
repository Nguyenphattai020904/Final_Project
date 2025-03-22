package com.example.final_project.Products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {
    private List<Product> topProducts;
    private Context context;
    private OnCarouselItemClickListener listener;

    public interface OnCarouselItemClickListener {
        void onCarouselItemClick(Product product);
    }

    public CarouselAdapter(Context context, List<Product> topProducts, OnCarouselItemClickListener listener) {
        this.context = context;
        this.topProducts = topProducts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.carousel_item, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Product product = topProducts.get(position);
        holder.productName.setText(product.getName());

        String imageUrl = product.getImages();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                if (imageUrl.startsWith("[")) {
                    String[] images = imageUrl.substring(1, imageUrl.length() - 1).split(",");
                    imageUrl = images[0].replace("\"", "").trim();
                }
                Picasso.get().load(imageUrl).into(holder.productImage);
            } catch (Exception e) {
                holder.productImage.setImageResource(R.drawable.img);
            }
        } else {
            holder.productImage.setImageResource(R.drawable.img);
        }

        // Thêm sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCarouselItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topProducts.size();
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.carousel_image);
            productName = itemView.findViewById(R.id.carousel_product_name);
        }
    }
}