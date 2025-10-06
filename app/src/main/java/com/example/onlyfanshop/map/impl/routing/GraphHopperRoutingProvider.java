package com.example.onlyfanshop.map.impl.routing;

import com.example.onlyfanshop.map.config.MapConfig;
import com.example.onlyfanshop.map.core.interfaces.RoutingProvider;
import com.example.onlyfanshop.map.models.RouteResult;
import com.example.onlyfanshop.map.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GraphHopperRoutingProvider implements RoutingProvider {

    // GH Directions GET: https://graphhopper.com/api/1/route?point=lat,lng&point=lat,lng&profile=car&key=YOUR_KEY&points_encoded=false
    private static final String BASE = "https://graphhopper.com/api/1/route";
    private String profile = "car";

    public void setProfile(String profile) { this.profile = profile; }

    @Override
    public void route(double sLat, double sLng, double eLat, double eLng, int alternatives, Callback cb) {
        String url = BASE + "?point=" + sLat + "," + sLng +
                "&point=" + eLat + "," + eLng +
                "&profile=" + profile +
                "&locale=en&points_encoded=false&instructions=false&key=" + MapConfig.GRAPH_HOPPER_KEY;
        HttpClient.get(url, new HttpClient.ResponseCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONObject root = new JSONObject(body);
                    JSONArray paths = root.getJSONArray("paths");
                    List<RouteResult> routes = new ArrayList<>();
                    for (int i = 0; i < paths.length(); i++) {
                        JSONObject pathObj = paths.getJSONObject(i);
                        double distance = pathObj.getDouble("distance");
                        double timeMs = pathObj.getDouble("time");
                        JSONObject points = pathObj.getJSONObject("points");
                        JSONArray coords = points.getJSONArray("coordinates");
                        List<double[]> pts = new ArrayList<>();
                        for (int j = 0; j < coords.length(); j++) {
                            JSONArray c = coords.getJSONArray(j);
                            pts.add(new double[]{c.getDouble(1), c.getDouble(0)});
                        }
                        RouteResult rr = new RouteResult();
                        rr.distanceMeters = distance;
                        rr.durationSeconds = timeMs / 1000.0;
                        rr.path = pts;
                        routes.add(rr);
                        if (routes.size() >= alternatives + 1) break;
                    }
                    cb.onSuccess(routes);
                } catch (Exception e) { cb.onError(e); }
            }
            @Override
            public void onError(Exception e) { cb.onError(e); }
        });
    }
}