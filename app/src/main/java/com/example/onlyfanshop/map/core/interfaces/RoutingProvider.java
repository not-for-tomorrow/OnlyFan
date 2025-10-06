package com.example.onlyfanshop.map.core.interfaces;

import com.example.onlyfanshop.map.models.RouteResult;

import java.util.List;

public interface RoutingProvider {
    void route(double sLat, double sLng, double eLat, double eLng, int alternatives, Callback cb);

    interface Callback {
        void onSuccess(List<RouteResult> routes);
        void onError(Throwable t);
    }
}