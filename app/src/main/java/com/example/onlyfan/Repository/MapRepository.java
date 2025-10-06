package com.example.onlyfan.Repository;

import androidx.annotation.MainThread;

import com.example.onlyfan.map.core.facade.MapServiceFacade;
import com.example.onlyfan.map.core.interfaces.AutocompleteProvider;
import com.example.onlyfan.map.core.interfaces.GeocodingProvider;
import com.example.onlyfan.map.core.interfaces.RoutingProvider;
import com.example.onlyfan.map.models.GeocodeResult;
import com.example.onlyfan.map.models.PlaceSuggestion;
import com.example.onlyfan.map.models.RouteResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Repository dùng để wrap facade (nếu sau này muốn caching / DB)
public class MapRepository {

    private final MapServiceFacade facade = new MapServiceFacade();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public interface SimpleCallback<T> {
        void onResult(T data, Throwable error);
    }

    public void geocode(String q, SimpleCallback<List<GeocodeResult>> cb) {
        facade.geocoding().geocode(q, new GeocodingProvider.Callback() {
            @Override
            public void onSuccess(List<GeocodeResult> results) {
                cb.onResult(results, null);
            }
            @Override
            public void onError(Throwable t) {
                cb.onResult(null, t);
            }
        });
    }

    public void route(double sLat,double sLng,double eLat,double eLng,int alt, SimpleCallback<List<RouteResult>> cb) {
        facade.routing().route(sLat, sLng, eLat, eLng, alt, new RoutingProvider.Callback() {
            @Override public void onSuccess(List<RouteResult> routes) { cb.onResult(routes, null); }
            @Override public void onError(Throwable t) { cb.onResult(null, t); }
        });
    }

    public void autocomplete(String q, SimpleCallback<List<PlaceSuggestion>> cb) {
        facade.autocomplete().suggest(q, new AutocompleteProvider.Callback() {
            @Override public void onSuccess(List<PlaceSuggestion> suggestions) { cb.onResult(suggestions, null); }
            @Override public void onError(Throwable t) { cb.onResult(null, t); }
        });
    }
}