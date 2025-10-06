package com.example.onlyfanshop.map.core.interfaces;

import com.example.onlyfanshop.map.models.PlaceSuggestion;

import java.util.List;

public interface AutocompleteProvider {
    void suggest(String query, Callback cb);
    interface Callback {
        void onSuccess(List<PlaceSuggestion> suggestions);
        void onError(Throwable t);
    }
}