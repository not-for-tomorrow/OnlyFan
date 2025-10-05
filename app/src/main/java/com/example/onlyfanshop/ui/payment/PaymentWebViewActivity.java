package com.example.onlyfanshop.ui.payment; // Hoặc package phù hợp với bạn

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.onlyfanshop.R;

public class PaymentWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_web_view);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        String url = getIntent().getStringExtra(EXTRA_URL);

        // Bật JavaScript (rất quan trọng cho các cổng thanh toán)
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Kiểm tra xem URL có phải là URL callback của bạn không
                if (url.contains("vn-pay-callback")) {
                    // Giao dịch đã hoàn tất (thành công hoặc thất bại)
                    //                    // Đóng WebView và trở về màn hình trước đó.
                    //                    // Bạn có thể truyền kết quả về nếu cần.
                    finish();
                    return true; // Đã xử lý URL, không cần WebView load nữa.
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }
}
    