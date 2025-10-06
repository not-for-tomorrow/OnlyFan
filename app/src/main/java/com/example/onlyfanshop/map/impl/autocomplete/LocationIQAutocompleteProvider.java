package com.example.onlyfanshop.map.impl.autocomplete;

import com.example.onlyfanshop.map.config.MapConfig;
import com.example.onlyfanshop.map.core.interfaces.AutocompleteProvider;
import com.example.onlyfanshop.map.models.PlaceSuggestion;
import com.example.onlyfanshop.map.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationIQAutocompleteProvider implements AutocompleteProvider {

    @Override
    public void suggest(String query, Callback cb) {
        String url = "https://us1.locationiq.com/v1/autocomplete.php?key=" +
                MapConfig.LOCATION_IQ_API_KEY + "&q=" + HttpClient.urlEncode(query) + "&limit=6";
        HttpClient.get(url, new HttpClient.ResponseCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONArray arr = new JSONArray(body);
                    List<PlaceSuggestion> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject r = arr.getJSONObject(i);
                        PlaceSuggestion ps = new PlaceSuggestion();
                        ps.id = r.optString("place_id");
                        ps.primaryText = r.optString("display_name");
                        ps.secondaryText = r.optString("type");
                        ps.lat = r.getDouble("lat");
                        ps.lng = r.getDouble("lon");
                        list.add(ps);
                    }
                    cb.onSuccess(list);
                } catch (Exception e){ cb.onError(e); }
            }
            @Override
            public void onError(Exception e){ cb.onError(e); }
        });
    }
}