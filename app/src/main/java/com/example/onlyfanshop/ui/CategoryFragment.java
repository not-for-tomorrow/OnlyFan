package com.example.onlyfanshop.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.adapter.CategoryAdapter;
import com.example.onlyfanshop.adapter.ProductAdapter;
import com.example.onlyfanshop.model.CategoryModel;
import com.example.onlyfanshop.model.ItemsModel;
import com.example.onlyfanshop.repository.MainRepository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private static final long DEBOUNCE_MS = 250;
    private static final boolean SHOW_ALL_WHEN_EMPTY = true;

    private EditText etSearch;
    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView empty;
    private SwipeRefreshLayout swipe;

    private ProductAdapter adapter;
    private final MainRepository repo = new MainRepository();

    private LiveData<ArrayList<ItemsModel>> liveAllProducts;

    private final List<ItemsModel> allCache = new ArrayList<>();
    private final List<ItemsModel> filtered = new ArrayList<>();

    private String currentQuery = "";
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingFilter;
    private RecyclerView categoryRecycler;
    private ProgressBar categoryProgress;
    private CategoryAdapter categoryAdapter;
    private LiveData<ArrayList<CategoryModel>> liveCategories;
    private final List<CategoryModel> categoryCache = new ArrayList<>();
    private String selectedCategory = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_category, container, false);
        bindViews(v);
        setupRecycler();
        setupCategoryRecycler();    // <-- PHẢI CÓ
        setupSearch();
        setupSwipe();
        loadCategories();           // <-- PHẢI CÓ
        loadAllProducts();
        return v;
    }

    private void bindViews(View v) {
        etSearch = v.findViewById(R.id.etSearchProduct);
        recycler = v.findViewById(R.id.recyclerSearchResult);
        progress = v.findViewById(R.id.progressSearch);
        empty = v.findViewById(R.id.textEmptySearch);
        swipe = v.findViewById(R.id.swipeSearchProducts);
        categoryRecycler = v.findViewById(R.id.categoryView);
        categoryProgress = v.findViewById(R.id.progressBarCategory);
    }

    private void setupRecycler() {
        adapter = new ProductAdapter(model -> {
            // TODO: Điều hướng sang màn chi tiết (sau này)
            // startActivity(new Intent(requireContext(), ProductDetailActivity.class).putExtra("item", model));
        });
        recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recycler.setAdapter(adapter);
    }

    private void setupCategoryRecycler() {
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), (model, pos) -> {
            filterByCategory(model.getTitle());
        });
        categoryRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        );
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString().trim();
                scheduleFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void scheduleFilter() {
        if (pendingFilter != null) debounceHandler.removeCallbacks(pendingFilter);
        pendingFilter = this::applyFilter;
        debounceHandler.postDelayed(pendingFilter, DEBOUNCE_MS);
    }

    private void setupSwipe() {
        if (swipe != null) {
            swipe.setOnRefreshListener(() -> {
                applyFilter();
                swipe.setRefreshing(false);
            });
            swipe.setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    android.R.color.holo_green_light
            );
        }
    }

    private void loadAllProducts() {
        showLoading(true);
        if (liveAllProducts == null) {
            liveAllProducts = repo.loadPopular();
            liveAllProducts.observe(getViewLifecycleOwner(), list -> {
                showLoading(false);
                allCache.clear();
                if (list != null) {
                    allCache.addAll(list);
                }
                applyFilter();
            });
        } else {
            showLoading(false);
            applyFilter();
        }
    }

    private void loadCategories() {
        showCategoryLoading(true);
        if (liveCategories == null) {
            liveCategories = new MainRepository().loadCategories();
            liveCategories.observe(getViewLifecycleOwner(), list -> {
                showCategoryLoading(false);
                categoryCache.clear();
                if (list != null) categoryCache.addAll(list);
                categoryAdapter.updateData(list);
            });
        } else {
            showCategoryLoading(false);
            categoryAdapter.updateData(categoryCache);
        }
    }

    private void showLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && empty != null) empty.setVisibility(View.GONE);
    }

    private void showCategoryLoading(boolean loading) {
        if (categoryProgress != null) categoryProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (categoryRecycler != null) categoryRecycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void applyFilter() {
        filtered.clear();
        String qNorm = normalize(currentQuery);

        for (ItemsModel m : allCache) {
            boolean matchCategory = selectedCategory.isEmpty() || normalize(m.getCategory()).equals(normalize(selectedCategory));
            boolean matchText = qNorm.isEmpty() || matches(m, qNorm);
            if (matchCategory && matchText) {
                filtered.add(m);
            }
        }
        render();
    }

    private boolean matches(ItemsModel m, String qNorm) {
        String title = normalize(m.getTitle());
        String brief = normalize(m.getBriefDescription());
        String full = normalize(m.getFullDescription());
        String category = normalize(m.getCategory());
        return title.contains(qNorm)
                || brief.contains(qNorm)
                || full.contains(qNorm)
                || category.contains(qNorm);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    private void render() {
        if (filtered.isEmpty()) {
            adapter.setData(new ArrayList<>());
            showEmpty(
                    currentQuery.isEmpty()
                            ? (SHOW_ALL_WHEN_EMPTY ? "Không có sản phẩm" : "Nhập để tìm kiếm")
                            : "Không tìm thấy sản phẩm"
            );
        } else {
            hideEmpty();
            adapter.setData(filtered);
        }
    }

    private void filterByCategory(String category) {
        if (category == null) category = "";
        // Toggle: nếu chọn lại category đang chọn thì bỏ lọc
        if (category.equals(selectedCategory)) {
            selectedCategory = "";
        } else {
            selectedCategory = category.trim();
        }
        applyFilter();
    }

    private void showEmpty(String msg) {
        if (empty != null) {
            empty.setText(msg);
            empty.setVisibility(View.VISIBLE);
        }
        if (recycler != null) recycler.setVisibility(View.INVISIBLE);
    }

    private void hideEmpty() {
        if (empty != null) empty.setVisibility(View.GONE);
        if (recycler != null) recycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingFilter != null) debounceHandler.removeCallbacks(pendingFilter);
    }
}