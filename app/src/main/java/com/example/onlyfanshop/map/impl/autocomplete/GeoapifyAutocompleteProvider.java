package com.example.onlyfanshop.map.impl.autocomplete;

import android.util.Log;

import com.example.onlyfanshop.map.config.MapConfig;
import com.example.onlyfanshop.map.core.interfaces.AutocompleteProvider;
import com.example.onlyfanshop.map.models.PlaceSuggestion;
import com.example.onlyfanshop.map.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeoapifyAutocompleteProvider implements AutocompleteProvider {

    @Override
    public void suggest(String query, Callback cb) {
        String url = "https://api.geoapify.com/v1/geocode/autocomplete?text=" +
                HttpClient.urlEncode(query) + "&limit=7&apiKey=" + MapConfig.GEOAPIFY_API_KEY;
        HttpClient.get(url, new HttpClient.ResponseCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONObject root = new JSONObject(body);

                    // Nếu API trả về lỗi quota hoặc key sai...
                    if (root.has("error")) {
                        String msg = root.optString("error", "Geoapify API error");
                        Log.e("GeoapifyAutocomplete", "API error: " + msg);
                        cb.onError(new Exception(msg));
                        return;
                    }

                    if (!root.has("features")) {
                        Log.e("GeoapifyAutocomplete", "No 'features' in response: " + body);
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }

                    JSONArray feats = root.getJSONArray("features");
                    List<PlaceSuggestion> list = new ArrayList<>();
                    for (int i = 0; i < feats.length(); i++) {
                        JSONObject f = feats.getJSONObject(i);
                        JSONObject props = f.optJSONObject("properties");
                        if (props == null) continue;
                        double lat = props.optDouble("lat", Double.NaN);
                        double lon = props.optDouble("lon", Double.NaN);
                        if (Double.isNaN(lat) || Double.isNaN(lon)) continue;
                        PlaceSuggestion ps = new PlaceSuggestion();
                        ps.id = props.optString("place_id", "");
                        ps.primaryText = props.optString("address_line1", props.optString("name", ""));
                        ps.secondaryText = props.optString("address_line2", "");
                        ps.lat = lat;
                        ps.lng = lon;
                        list.add(ps);
                    }
                    cb.onSuccess(list);
                } catch (Exception e){
                    Log.e("GeoapifyAutocomplete", "Parse error: " + e.getMessage());
                    cb.onError(e);
                }
            }
            @Override
            public void onError(Exception e){
                Log.e("GeoapifyAutocomplete", "Network/API error: " + e.getMessage());
                cb.onError(e);
            }
        });
    }
}