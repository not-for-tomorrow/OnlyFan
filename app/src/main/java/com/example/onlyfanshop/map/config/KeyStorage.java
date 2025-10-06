package com.example.onlyfanshop.map.config;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyStorage {

    private static final String PREF = "map_api_keys";

    public static void save(Context ctx,
                            String oc, String geo, String locIq,
                            String ors, String gh) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putString("OPEN_CAGE", oc)
                .putString("GEOAPIFY", geo)
                .putString("LOCATION_IQ", locIq)
                .putString("ORS", ors)
                .putString("GRAPH_HOPPER", gh)
                .apply();
    }

    public static void loadIntoConfig(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String oc = sp.getString("OPEN_CAGE", null);
        String geo = sp.getString("GEOAPIFY", null);
        String loc = sp.getString("LOCATION_IQ", null);
        String ors = sp.getString("ORS", null);
        String gh  = sp.getString("GRAPH_HOPPER", null);

        if (oc != null && !oc.isEmpty()) MapConfig.OPEN_CAGE_API_KEY = oc;
        if (geo != null && !geo.isEmpty()) MapConfig.GEOAPIFY_API_KEY = geo;
        if (loc != null && !loc.isEmpty()) MapConfig.LOCATION_IQ_API_KEY = loc;
        if (ors != null && !ors.isEmpty()) MapConfig.OPEN_ROUTE_SERVICE_KEY = ors;
        if (gh != null && !gh.isEmpty()) MapConfig.GRAPH_HOPPER_KEY = gh;
    }
}