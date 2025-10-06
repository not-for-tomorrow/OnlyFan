package com.example.onlyfan.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlyfan.Repository.MapRepository;
import com.example.onlyfan.map.models.GeocodeResult;
import com.example.onlyfan.map.models.PlaceSuggestion;
import com.example.onlyfan.map.models.RouteResult;

import java.util.List;

public class MapViewModel extends ViewModel {

    private final MapRepository repository = new MapRepository();

    private final MutableLiveData<List<PlaceSuggestion>> suggestions = new MutableLiveData<>();
    private final MutableLiveData<List<GeocodeResult>> geocodeResults = new MutableLiveData<>();
    private final MutableLiveData<List<RouteResult>> routeResults = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<PlaceSuggestion>> getSuggestions() { return suggestions; }
    public LiveData<List<GeocodeResult>> getGeocodeResults(){ return geocodeResults; }
    public LiveData<List<RouteResult>> getRouteResults(){ return routeResults; }
    public LiveData<String> getError(){ return error; }

    public void search(String q){
        repository.geocode(q, (data, err) -> {
            if (err != null) error.postValue(err.getMessage());
            else geocodeResults.postValue(data);
        });
    }

    public void autoComplete(String q){
        repository.autocomplete(q, (data, err) -> {
            if (err != null) return; // im lặng
            suggestions.postValue(data);
        });
    }

    public void route(double sLat,double sLng,double eLat,double eLng,int alt){
        repository.route(sLat, sLng, eLat, eLng, alt, (data, err) -> {
            if (err != null) error.postValue(err.getMessage());
            else routeResults.postValue(data);
        });
    }
}