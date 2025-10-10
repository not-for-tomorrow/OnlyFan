package com.example.onlyfanshop.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductFilterViewModel extends ViewModel {
    private final MutableLiveData<String> keyword = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> categoryId = new MutableLiveData<>(null);

    public LiveData<String> getKeyword() { return keyword; }
    public LiveData<Integer> getCategoryId() { return categoryId; }

    public void setKeyword(String value) {
        // Tránh phát lại giá trị giống nhau
        String cur = keyword.getValue();
        if ((cur == null && value == null) || (cur != null && cur.equals(value))) return;
        keyword.setValue(value);
    }

    public void setCategoryId(Integer value) {
        Integer cur = categoryId.getValue();
        if ((cur == null && value == null) || (cur != null && cur.equals(value))) return;
        categoryId.setValue(value);
    }
}