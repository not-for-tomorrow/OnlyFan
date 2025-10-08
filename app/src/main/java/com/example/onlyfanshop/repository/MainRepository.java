package com.example.onlyfanshop.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlyfanshop.model.BannerModel;
import com.example.onlyfanshop.model.CategoryModel;
import com.example.onlyfanshop.model.ItemsModel;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainRepository {

    private static final String DB_URL =
            "https://onlyfan-f9406-default-rtdb.asia-southeast1.firebasedatabase.app";

    // Dùng instance với URL đúng region
    private final FirebaseDatabase firebaseDatabase =
            FirebaseDatabase.getInstance(DB_URL);

    // Lưu lại listener nếu bạn muốn gỡ sau này (tránh leak khi Fragment destroy)
    private final List<ValueEventListener> activeListeners = new ArrayList<>();

    public LiveData<ArrayList<CategoryModel>> loadCategories() {
        MutableLiveData<ArrayList<CategoryModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Category");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CategoryModel> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CategoryModel item = dataSnapshot.getValue(CategoryModel.class);
                    if (item != null) {
                        Log.d("FIREBASE", "Category: " + item.getTitle());
                        list.add(item);
                    }
                }
                Log.d("FIREBASE", "Total categories = " + list.size());
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Lỗi đọc Category: " + error.getMessage());
            }
        };
        ref.addValueEventListener(listener);
        activeListeners.add(listener);
        return listData;
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        MutableLiveData<ArrayList<BannerModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Banner");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<BannerModel> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BannerModel item = dataSnapshot.getValue(BannerModel.class);
                    if (item != null) {
                        list.add(item);
                    }
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Lỗi đọc Banner: " + error.getMessage());
            }
        };
        ref.addValueEventListener(listener);
        activeListeners.add(listener);
        return listData;
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        MutableLiveData<ArrayList<ItemsModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Items");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemsModel item = dataSnapshot.getValue(ItemsModel.class);
                    if (item != null) {
                        list.add(item);
                    }
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Lỗi đọc Items: " + error.getMessage());
            }
        };
        ref.addValueEventListener(listener);
        activeListeners.add(listener);
        return listData;
    }

    // (Tuỳ chọn) Gọi trong ViewModel.onCleared() để remove listeners nếu cần
    public void clear() {
        for (ValueEventListener l : activeListeners) {
            // Không có ref trực tiếp ở đây để remove; nếu cần remove chính xác,
            // bạn có thể lưu cặp (DatabaseReference, ValueEventListener)
        }
        activeListeners.clear();
    }
}