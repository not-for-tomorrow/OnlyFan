package com.example.onlyfanshop.ui.product;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.onlyfanshop.R;
import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.ProductApi;
import com.example.onlyfanshop.model.ProductDetailDTO;
import com.example.onlyfanshop.model.response.ApiResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ImageView imageProduct;
    private TextView textBrand, textProductName, textBottomPrice, textBrief, textFull, textSpecs;
    private MaterialButton btnAddToCart;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        imageProduct = findViewById(R.id.imageProduct);
        textBrand = findViewById(R.id.textBrand);
        textProductName = findViewById(R.id.textProductName);
        textBottomPrice = findViewById(R.id.textBottomPrice);
        textBrief = findViewById(R.id.textBrief);
        textFull = findViewById(R.id.textFull);
        textSpecs = findViewById(R.id.textSpecs);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        findViewById(R.id.btnFavorite).setOnClickListener(v -> toggleFavorite());

        int id = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (id > 0) {
            fetchDetail(id);
        }
    }

    private void fetchDetail(int id) {
        ProductApi api = ApiClient.getClient().create(ProductApi.class);
        api.getProductDetail(id).enqueue(new Callback<ApiResponse<ProductDetailDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductDetailDTO>> call, Response<ApiResponse<ProductDetailDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductDetailDTO d = response.body().getData();
                    if (d == null) return;
                    textBrand.setText(d.getBrand() != null ? d.getBrand().getName() : "");
                    textProductName.setText(d.getProductName());
                    textBottomPrice.setText(String.format("$%.2f", d.getPrice() != null ? d.getPrice() : 0));
                    textBrief.setText(d.getBriefDescription());
                    textFull.setText(d.getFullDescription());
                    textSpecs.setText(d.getTechnicalSpecifications());
                    Glide.with(ProductDetailActivity.this)
                            .load(d.getImageURL())
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(imageProduct);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductDetailDTO>> call, Throwable t) {
                // Optionally show a toast/snackbar
            }
        });
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        ImageView fav = findViewById(R.id.btnFavorite);
        fav.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        // TODO: Optionally call backend to persist favorite state
    }
}


