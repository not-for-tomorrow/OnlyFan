package com.example.onlyfanshop.map.impl.routing;

import com.example.onlyfanshop.map.config.MapConfig;
import com.example.onlyfanshop.map.core.interfaces.RoutingProvider;
import com.example.onlyfanshop.map.models.RouteResult;
import com.example.onlyfanshop.map.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OpenRouteServiceRoutingProvider implements RoutingProvider {

    private static final String BASE = "https://api.openrouteservice.org/v2/directions/";
    private String profile = "driving-car";

    public void setProfile(String profile) { this.profile = profile; }

    @Override
    public void route(double sLat, double sLng, double eLat, double eLng, int alternatives, Callback cb) {
        String url = BASE + profile;
        try {
            JSONObject body = new JSONObject();
            JSONArray coords = new JSONArray();
            coords.put(new JSONArray().put(sLng).put(sLat));
            coords.put(new JSONArray().put(eLng).put(eLat));
            body.put("coordinates", coords);
            body.put("instructions", true);

            HttpClient.postJson(url, body.toString(), MapConfig.OPEN_ROUTE_SERVICE_KEY,
                    new HttpClient.ResponseCallback() {
                        @Override
                        public void onSuccess(String resp) {
                            try {
                                JSONObject root = new JSONObject(resp);
                                JSONArray features = root.getJSONArray("features");
                                List<RouteResult> routes = new ArrayList<>();
                                for (int i = 0; i < features.length(); i++) {
                                    JSONObject feat = features.getJSONObject(i);
                                    JSONObject props = feat.getJSONObject("properties");
                                    JSONObject summary = props.getJSONObject("summary");
                                    double distance = summary.getDouble("distance");
                                    double duration = summary.getDouble("duration");
                                    JSONObject geometry = feat.getJSONObject("geometry");
                                    JSONArray cArr = geometry.getJSONArray("coordinates");

                                    List<double[]> pts = new ArrayList<>();
                                    for (int j = 0; j < cArr.length(); j++) {
                                        JSONArray c = cArr.getJSONArray(j);
                                        pts.add(new double[]{c.getDouble(1), c.getDouble(0)});
                                    }

                                    RouteResult rr = new RouteResult();
                                    rr.distanceMeters = distance;
                                    rr.durationSeconds = duration;
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
        } catch (Exception e) {
            cb.onError(e);
        }
    }
}