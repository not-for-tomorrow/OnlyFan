package com.example.onlyfan.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlyfan.Domain.BannerModel;
import com.example.onlyfan.Domain.CategoryModel;
import com.example.onlyfan.Domain.ItemsModel;
import com.example.onlyfan.Repository.MainRepository;

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
