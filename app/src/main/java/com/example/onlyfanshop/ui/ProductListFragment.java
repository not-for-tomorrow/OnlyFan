package com.example.onlyfanshop.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.adapter.ProductAdapter;
import com.example.onlyfanshop.model.ItemsModel;
import com.example.onlyfanshop.repository.MainRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Hiển thị danh sách sản phẩm thuộc một danh mục.
 * Lưu ý: MainRepository hiện chỉ có loadPopular() (lấy toàn bộ Items),
 * nên fragment này tự lọc theo categoryName.
 */
public class ProductListFragment extends Fragment {

    private static final String ARG_CATEGORY_NAME = "arg_category_name";

    // Danh mục cần hiển thị
    private String categoryName;

    // UI
    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView empty;
    private SwipeRefreshLayout swipe;

    // Adapter & dữ liệu
    private ProductAdapter adapter;
    private final List<ItemsModel> allItemsCache = new ArrayList<>(); // cache tất cả (từ Firebase)
    private final List<ItemsModel> filtered = new ArrayList<>();

    // Repository
    private final MainRepository repo = new MainRepository();
    private LiveData<ArrayList<ItemsModel>> liveDataSource;

    // Handler cho refresh giả lập (trong trường hợp user kéo nhưng data đã realtime)
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static ProductListFragment newInstance(String categoryName) {
        ProductListFragment f = new ProductListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CATEGORY_NAME, categoryName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryName = getArguments() != null ? getArguments().getString(ARG_CATEGORY_NAME) : null;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_product_list, container, false);
        bindViews(v);
        setupRecycler();
        setupSwipeRefresh();
        loadDataIfNeeded();
        return v;
    }

    private void bindViews(View v) {
        recycler = v.findViewById(R.id.recyclerProducts);
        progress = v.findViewById(R.id.progressProducts);
        empty = v.findViewById(R.id.textEmptyProducts);
        swipe = v.findViewById(R.id.swipeRefreshProducts);
    }

    private void setupRecycler() {
        adapter = new ProductAdapter(model -> {
            // TODO: mở màn hình chi tiết sản phẩm sau này
            // Ví dụ:
            // startActivity(new Intent(requireContext(), ProductDetailActivity.class)
            //        .putExtra("item", model));
        });
        recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recycler.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipe.setOnRefreshListener(() -> {
            // Vì loadPopular() dùng ValueEventListener realtime, dữ liệu sẽ tự cập nhật.
            // Ở đây chỉ tái áp dụng filter & tắt hiệu ứng sau một chút.
            applyFilter();
            handler.postDelayed(() -> {
                if (swipe != null) swipe.setRefreshing(false);
            }, 600);
        });
    }

    private void loadDataIfNeeded() {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            showEmpty("Không có danh mục");
            return;
        }
        showLoading(true);

        // Chỉ đăng ký observer một lần
        if (liveDataSource == null) {
            liveDataSource = repo.loadPopular();
            liveDataSource.observe(getViewLifecycleOwner(), items -> {
                if (items == null) {
                    showEmpty("Không có sản phẩm");
                    return;
                }
                allItemsCache.clear();
                allItemsCache.addAll(items);
                applyFilter();
            });
        } else {
            // Nếu đã có live data (quay lại fragment), chỉ cần áp lại filter
            applyFilter();
        }
    }

    /**
     * Lọc danh sách theo categoryName (case-insensitive).
     */
    private void applyFilter() {
        filtered.clear();
        if (!TextUtils.isEmpty(categoryName)) {
            for (ItemsModel m : allItemsCache) {
                if (m.getCategory() != null &&
                        m.getCategory().equalsIgnoreCase(categoryName)) {
                    filtered.add(m);
                }
            }
        }
        renderFiltered();
    }

    private void renderFiltered() {
        showLoading(false);
        if (filtered.isEmpty()) {
            adapter.setData(new ArrayList<>());
            showEmpty("Không có sản phẩm");
        } else {
            hideEmpty();
            adapter.setData(filtered);
        }
    }

    private void showLoading(boolean loading) {
        if (progress != null) {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmpty(String msg) {
        showLoading(false);
        if (empty != null) {
            empty.setText(msg);
            empty.setVisibility(View.VISIBLE);
        }
        if (recycler != null) {
            recycler.setVisibility(View.INVISIBLE);
        }
    }

    private void hideEmpty() {
        if (empty != null) empty.setVisibility(View.GONE);
        if (recycler != null) recycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}