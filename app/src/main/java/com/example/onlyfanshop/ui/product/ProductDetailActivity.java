package com.example.onlyfanshop.ui.product;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.onlyfanshop.R;
import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.PaymentApi;
import com.example.onlyfanshop.api.ProductApi;
import com.example.onlyfanshop.model.PaymentDTO;
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
    private ProgressBar progressBar;
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
        progressBar = findViewById(R.id.progressBar);

        btnAddToCart.setOnClickListener(v -> testPayment());

        findViewById(R.id.btnFavorite).setOnClickListener(v -> toggleFavorite());

        int id = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (id > 0) {
            fetchDetail(id);
        }
    }

    private void testPayment() {
        // Giả sử bạn lấy giá từ TextView hoặc một biến thành viên đã lưu giá sản phẩm
        // Ví dụ: lấy giá từ textBottomPrice
        String priceString = textBottomPrice.getText().toString().replace("$", "");
        double amount;
        try {
            amount = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid product price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mã ngân hàng để test, bạn có thể thay đổi hoặc cho người dùng chọn
        String bankCode = "VNBANK";

        Log.d("Payment", "Creating payment with amount: " + amount + " and bankCode: " + bankCode);
        showLoading(true);

        PaymentApi api = ApiClient.getPrivateClient(this).create(PaymentApi.class);
        api.createPayment(amount, bankCode).enqueue(new Callback<ApiResponse<PaymentDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentDTO>> call, Response<ApiResponse<PaymentDTO>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Xử lý khi gọi API thành công, ví dụ: mở URL thanh toán
                    String paymentUrl = response.body().getData().getPaymentUrl(); // Giả sử response có trường 'url'
                    Log.d("Payment", "Payment URL: " + paymentUrl);
                    Toast.makeText(ProductDetailActivity.this, "Redirecting to payment...", Toast.LENGTH_SHORT).show();

                    // Mở URL thanh toán bằng trình duyệt
                    // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                    // startActivity(browserIntent);
                } else {
                    // Xử lý khi có lỗi từ server
                    Log.e("Payment", "API call failed with response code: " + response.code());
                    Toast.makeText(ProductDetailActivity.this, "Failed to create payment.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentDTO>> call, Throwable t) {
                showLoading(false);
                // Xử lý khi có lỗi mạng
                Log.e("Payment", "Network error: " + t.getMessage(), t);
                Toast.makeText(ProductDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDetail(int id) {
        Log.d("ProductDetail", "Fetching product detail for ID: " + id);
        showLoading(true);
        ProductApi api = ApiClient.getPrivateClient(this).create(ProductApi.class);
        api.getProductDetail(id).enqueue(new Callback<ApiResponse<ProductDetailDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductDetailDTO>> call, Response<ApiResponse<ProductDetailDTO>> response) {
                showLoading(false);
                Log.d("ProductDetail", "Response code: " + response.code());
                Log.d("ProductDetail", "Response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    ProductDetailDTO d = response.body().getData();
                    Log.d("ProductDetail", "Product data: " + d);
                    if (d == null) {
                        Log.e("ProductDetail", "Product data is null");
                        Toast.makeText(ProductDetailActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bindProductData(d);
                } else {
                    Log.e("ProductDetail", "Response not successful or body is null");
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductDetailDTO>> call, Throwable t) {
                showLoading(false);
                Log.e("ProductDetail", "Network error: " + t.getMessage(), t);
                Toast.makeText(ProductDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProductData(ProductDetailDTO product) {
        textBrand.setText(product.getBrand() != null ? product.getBrand().getName() : "");
        textProductName.setText(product.getProductName());
        textBottomPrice.setText(String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0));
        textBrief.setText(product.getBriefDescription() != null ? product.getBriefDescription() : "");
        textFull.setText(product.getFullDescription() != null ? product.getFullDescription() : "");
        textSpecs.setText(product.getTechnicalSpecifications() != null ? product.getTechnicalSpecifications() : "");
        
        if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
            Glide.with(ProductDetailActivity.this)
                    .load(product.getImageURL())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageProduct);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        ImageView fav = findViewById(R.id.btnFavorite);
        fav.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        // TODO: Optionally call backend to persist favorite state
    }
}


