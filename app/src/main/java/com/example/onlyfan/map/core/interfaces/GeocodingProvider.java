package com.example.onlyfan.map.core.interfaces;

import com.example.onlyfan.map.models.GeocodeResult;
import java.util.List;

public interface GeocodingProvider {
    void geocode(String q, Callback cb);
    void reverse(double lat, double lng, Callback cb);

    interface Callback {
        void onSuccess(List<GeocodeResult> results);
        void onError(Throwable t);
    }
}