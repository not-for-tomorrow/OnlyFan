package com.example.onlyfanshop.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlyfanshop.model.BannerModel;
import com.example.onlyfanshop.model.CategoryModel;
import com.example.onlyfanshop.model.ItemsModel;
import com.example.onlyfanshop.repository.MainRepository;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MainRepository repository = new MainRepository();

    public LiveData<ArrayList<CategoryModel>> loadCategories() {
        return repository.loadCategories();
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        return repository.loadBanner();
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        return repository.loadPopular();
    }
}
