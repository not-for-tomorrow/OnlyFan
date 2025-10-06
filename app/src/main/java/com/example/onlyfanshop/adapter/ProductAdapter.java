package com.example.onlyfanshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlyfanshop.R;
import com.example.onlyfanshop.model.ItemsModel;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductVH> {

    public interface OnProductClick {
        void onClick(ItemsModel model);
    }

    private final List<ItemsModel> data = new ArrayList<>();
    private final OnProductClick listener;

    public ProductAdapter(OnProductClick listener) {
        this.listener = listener;
    }

    public void setData(List<ItemsModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductVH h, int position) {
        ItemsModel m = data.get(position);
        h.title.setText(m.getTitle());
        h.price.setText(m.getPrice());
        h.sold.setText("Đã bán " + fakeSold(m));
        h.location.setText("Việt Nam");
        Glide.with(h.itemView.getContext())
                .load(m.getImageUrl())
                .placeholder(R.drawable.cat4)
                .into(h.img);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(m);
        });
    }

    private String fakeSold(ItemsModel m) {
        // Bạn có thể thay bằng trường thật nếu sau này có
        int base = Math.abs(m.getTitle().hashCode()) % 1500 + 50;
        if (base > 999)
            return (base / 1000.0f) + "k";
        return String.valueOf(base);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ProductVH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, price, sold, location;
        ProductVH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageProduct);
            title = itemView.findViewById(R.id.textTitle);
            price = itemView.findViewById(R.id.textPrice);
            sold = itemView.findViewById(R.id.textSold);
            location = itemView.findViewById(R.id.textLocation);
        }
    }
}
