package com.example.onlyfanshop.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.adapter.BannerAdapter;
import com.example.onlyfanshop.adapter.PopularAdapter;
import com.example.onlyfanshop.api.ApiClient;
import com.example.onlyfanshop.api.ProductApi;
import com.example.onlyfanshop.model.BannerModel;
import com.example.onlyfanshop.model.ProductDTO;
import com.example.onlyfanshop.model.response.ApiResponse;
import com.example.onlyfanshop.model.response.HomePageData;
import com.example.onlyfanshop.ui.product.ProductDetailActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // URL instance của Realtime Database (đúng như ảnh bạn gửi)
    private static final String DB_URL = "https://onlyfan-f9406-default-rtdb.asia-southeast1.firebasedatabase.app";
    // Tên node đúng phân biệt hoa/thường (ảnh chụp là “Banner”)
    private static final String BANNER_NODE = "Banner";

    // Banner
    private ViewPager2 viewPagerBanner;
    private ProgressBar progressBarBanner;
    private BannerAdapter bannerAdapter;
    private final Handler sliderHandler = new Handler();
    private static final long SLIDER_INTERVAL_MS = 3000L;

    // Popular
    private RecyclerView popularView;
    private ProgressBar progressBarPopular;
    private PopularAdapter popularAdapter;
    private ProductApi productApi;

    private final Runnable sliderRunnable = new Runnable() {
        @Override public void run() {
            if (!isAdded() || viewPagerBanner == null || bannerAdapter == null) return;
            int count = bannerAdapter.getItemCount();
            if (count <= 1) return;
            int next = viewPagerBanner.getCurrentItem() + 1;
            if (next >= count) next = 0;
            viewPagerBanner.setCurrentItem(next, true);
            sliderHandler.postDelayed(this, SLIDER_INTERVAL_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Banner
        viewPagerBanner = v.findViewById(R.id.viewPagerBanner);
        progressBarBanner = v.findViewById(R.id.progressBarBanner);

        bannerAdapter = new BannerAdapter(new ArrayList<>(), viewPagerBanner);
        viewPagerBanner.setAdapter(bannerAdapter);

        viewPagerBanner.setClipToPadding(false);
        viewPagerBanner.setClipChildren(false);
        viewPagerBanner.setOffscreenPageLimit(3);
        CompositePageTransformer composite = new CompositePageTransformer();
        composite.addTransformer(new MarginPageTransformer(40));
        viewPagerBanner.setPageTransformer(composite);

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_INTERVAL_MS);
            }
        });

        // Load banner từ Realtime Database (KHÔNG còn đọc từ Storage)
        loadBannersFromRealtimeDb();

        // Popular
        popularView = v.findViewById(R.id.popularView);
        progressBarPopular = v.findViewById(R.id.progressBarPopular);

        popularView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        popularView.setNestedScrollingEnabled(false);

        popularAdapter = new PopularAdapter(item -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, item.getProductID());
            startActivity(intent);
        });
        popularView.setAdapter(popularAdapter);

        productApi = ApiClient.getPrivateClient(requireContext()).create(ProductApi.class);
        loadPopular();
    }

    // -------- Banner từ Realtime Database --------
    private void setBannerLoading(boolean loading) {
        if (progressBarBanner != null) progressBarBanner.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadBannersFromRealtimeDb() {
        setBannerLoading(true);

        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference()
                .child(BANNER_NODE);

        // Đọc 1 lần (get). Có thể đổi sang addValueEventListener nếu muốn realtime.
        ref.get().addOnSuccessListener(snapshot -> {
            ArrayList<BannerModel> banners = new ArrayList<>();
            // Hỗ trợ cả 2 dạng:
            // - Banner: { "0": { "url": "https://..." }, "1": { "url": "..." } }
            // - Banner: { "0": "https://...", "1": "https://..." }
            for (DataSnapshot child : snapshot.getChildren()) {
                String url = null;
                if (child.hasChild("url")) {
                    url = child.child("url").getValue(String.class);
                } else {
                    url = child.getValue(String.class);
                }
                if (url != null && !url.isEmpty()) {
                    BannerModel m = new BannerModel();
                    m.setUrl(url);
                    banners.add(m);
                }
            }
            bannerAdapter.submit(banners);
            setBannerLoading(false);
            if (!banners.isEmpty()) startAutoSlide();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Realtime DB load failed at node '" + BANNER_NODE + "'", e);
            bannerAdapter.submit(new ArrayList<>());
            setBannerLoading(false);
        });
    }

    private void startAutoSlide() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, SLIDER_INTERVAL_MS);
    }

    private void stopAutoSlide() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override public void onResume() {
        super.onResume();
        startAutoSlide();
    }

    @Override public void onPause() {
        stopAutoSlide();
        super.onPause();
    }

    @Override public void onDestroyView() {
        stopAutoSlide();
        super.onDestroyView();
    }

    // ---------------- Popular ----------------
    private void setPopularLoading(boolean loading) {
        if (progressBarPopular != null) {
            progressBarPopular.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void loadPopular() {
        setPopularLoading(true);
        productApi.getHomePagePost(1, 10, "ProductID", "DESC", null, null, null)
                .enqueue(new Callback<ApiResponse<HomePageData>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<HomePageData>> call,
                                           @NonNull Response<ApiResponse<HomePageData>> response) {
                        setPopularLoading(false);
                        List<ProductDTO> products = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            if (response.body().getData().products != null) {
                                products = response.body().getData().products;
                            }
                        }
                        popularAdapter.submitList(products);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<HomePageData>> call, @NonNull Throwable t) {
                        setPopularLoading(false);
                        popularAdapter.submitList(new ArrayList<>());
                    }
                });
    }
}