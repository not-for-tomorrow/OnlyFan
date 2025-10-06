package com.example.onlyfanshop.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.onlyfanshop.ViewModel.MainViewModel;
import com.example.onlyfanshop.adapter.BannerAdapter;
import com.example.onlyfanshop.adapter.PopularAdapter;
import com.example.onlyfanshop.databinding.FragmentHomeBinding;
import com.example.onlyfanshop.model.BannerModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainViewModel viewModel;
    private final Handler sliderHandler = new Handler();

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding != null) {
                int currentItem = binding.viewPagerBanner.getCurrentItem();
                binding.viewPagerBanner.setCurrentItem(currentItem + 1);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new MainViewModel();
        initBanner();
        initPopular();
        return binding.getRoot();
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.loadPopular().observe(getViewLifecycleOwner(), itemsModels -> {
            if (itemsModels != null && !itemsModels.isEmpty()) {
                binding.popularView.setLayoutManager(
                        new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                );
                binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                binding.popularView.setNestedScrollingEnabled(true);
            }
            binding.progressBarPopular.setVisibility(View.GONE);
        });
        viewModel.loadPopular();
    }

    private void banner(ArrayList<BannerModel> bannerModels) {
        binding.viewPagerBanner.setAdapter(new BannerAdapter(bannerModels, binding.viewPagerBanner));
        binding.viewPagerBanner.setClipToPadding(false);
        binding.viewPagerBanner.setClipChildren(false);
        binding.viewPagerBanner.setOffscreenPageLimit(3);
        binding.viewPagerBanner.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerBanner.setPageTransformer(compositePageTransformer);

        binding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void initBanner() {
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        viewModel.loadBanner().observe(getViewLifecycleOwner(), bannerModels -> {
            if (bannerModels != null && !bannerModels.isEmpty()) {
                banner(bannerModels);
            }
            binding.progressBarBanner.setVisibility(View.GONE);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        binding = null;
    }
}