package com.example.onlyfanshop.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.ViewModel.MapViewModel;
import com.example.onlyfanshop.map.config.KeyStorage;
import com.example.onlyfanshop.map.config.MapConfig;
import com.example.onlyfanshop.map.core.interfaces.MapProvider;
import com.example.onlyfanshop.map.impl.map.OsmMapProvider;
import com.example.onlyfanshop.map.models.GeocodeResult;
import com.example.onlyfanshop.map.models.PlaceSuggestion;
import com.example.onlyfanshop.map.models.RouteResult;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapViewModel vm;
    private MapProvider mapProvider;

    private EditText etSearch;
    private ImageButton btnSearch, btnToggleKeyPanel;
    private ListView lvSuggestions;
    private TextView tvRouteInfo;
    private Button btnClearRoute, btnApplyKeys;
    private View apiKeyPanel;
    private EditText etOpenCageKey, etGeoapifyKey, etLocationIqKey, etOrsKey, etGraphHopperKey;

    private final List<PlaceSuggestion> currentSuggestions = new ArrayList<>();
    private ArrayAdapter<String> suggestionsAdapter;
    private double[] routeStart = null;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle savedInstanceState){
        super.onViewCreated(v, savedInstanceState);
        vm = new ViewModelProvider(this).get(MapViewModel.class);

        etSearch = v.findViewById(R.id.etSearch);
        btnSearch = v.findViewById(R.id.btnSearch);
        lvSuggestions = v.findViewById(R.id.lvSuggestions);
        tvRouteInfo = v.findViewById(R.id.tvRouteInfo);
        btnClearRoute = v.findViewById(R.id.btnClearRoute);
        btnToggleKeyPanel = v.findViewById(R.id.btnToggleKeyPanel);
        apiKeyPanel = v.findViewById(R.id.apiKeyPanel);
        etOpenCageKey = v.findViewById(R.id.etOpenCageKey);
        etGeoapifyKey = v.findViewById(R.id.etGeoapifyKey);
        etLocationIqKey = v.findViewById(R.id.etLocationIqKey);
        etOrsKey = v.findViewById(R.id.etOrsKey);
        etGraphHopperKey = v.findViewById(R.id.etGraphHopperKey);
        btnApplyKeys = v.findViewById(R.id.btnApplyKeys);

        // Load key đã lưu
        KeyStorage.loadIntoConfig(requireContext());

        initMap(v);
        bindViewModel();
        bindEvents();
    }

    private void initMap(View root){
        mapProvider = new OsmMapProvider();
        FrameLayout container = root.findViewById(R.id.mapContainer);
        View mapView = mapProvider.createMapView(requireContext());
        container.addView(mapView);
        mapProvider.moveCamera(10.762622, 106.660172, 12);

        mapProvider.setOnMapClickListener((lat, lng) -> lvSuggestions.setVisibility(View.GONE));
        mapProvider.setOnMapLongClickListener((lat, lng) -> {
            if (routeStart == null){
                routeStart = new double[]{lat, lng};
                mapProvider.addMarker("start", lat, lng, "Start", "");
                Toast.makeText(getContext(), "Đã chọn điểm bắt đầu", Toast.LENGTH_SHORT).show();
            } else {
                mapProvider.addMarker("end", lat, lng, "End", "");
                vm.route(routeStart[0], routeStart[1], lat, lng, MapConfig.ROUTE_MAX_ALTERNATIVES);
                routeStart = null;
            }
        });
    }

    private void bindViewModel(){
        vm.getGeocodeResults().observe(getViewLifecycleOwner(), results -> {
            if (results == null || results.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy", Toast.LENGTH_SHORT).show();
                return;
            }
            GeocodeResult r = results.get(0);
            mapProvider.addMarker("search", r.lat, r.lng, r.formattedAddress, "");
            mapProvider.moveCamera(r.lat, r.lng, 15);
        });

        vm.getSuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            if (suggestions == null) return;
            currentSuggestions.clear();
            currentSuggestions.addAll(suggestions);
            List<String> labels = new ArrayList<>();
            for (PlaceSuggestion p : suggestions) {
                labels.add(p.primaryText + (p.secondaryText != null ? " - " + p.secondaryText : ""));
            }
            if (suggestionsAdapter == null){
                suggestionsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, labels);
                lvSuggestions.setAdapter(suggestionsAdapter);
            } else {
                suggestionsAdapter.clear();
                suggestionsAdapter.addAll(labels);
                suggestionsAdapter.notifyDataSetChanged();
            }
            lvSuggestions.setVisibility(View.VISIBLE);
        });

        vm.getRouteResults().observe(getViewLifecycleOwner(), routes -> {
            if (routes == null || routes.isEmpty()) return;
            RouteResult main = routes.get(0);
            mapProvider.addPolyline("route_main", main.path, 0xFF0066FF, 8f);
            tvRouteInfo.setText(String.format("Dist: %.1f km | Time: %.1f min",
                    main.distanceMeters / 1000.0, main.durationSeconds / 60.0));
        });

        vm.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(getContext(), "Lỗi: " + err, Toast.LENGTH_SHORT).show();
        });
    }

    private void bindEvents(){
        btnSearch.setOnClickListener(v -> vm.search(etSearch.getText().toString()));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                if (s.length() > 2) vm.autoComplete(s.toString());
                else lvSuggestions.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s){}
        });

        lvSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            PlaceSuggestion ps = currentSuggestions.get(position);
            lvSuggestions.setVisibility(View.GONE);
            mapProvider.addMarker("search", ps.lat, ps.lng, ps.primaryText, ps.secondaryText);
            mapProvider.moveCamera(ps.lat, ps.lng, 15);
        });

        btnClearRoute.setOnClickListener(v -> {
            mapProvider.clearPolyline("route_main");
            mapProvider.removeMarker("start");
            mapProvider.removeMarker("end");
            tvRouteInfo.setText("Chưa có route");
        });

        btnToggleKeyPanel.setOnClickListener(v -> {
            apiKeyPanel.setVisibility(apiKeyPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        btnApplyKeys.setOnClickListener(v -> {
            KeyStorage.save(requireContext(),
                    etOpenCageKey.getText().toString(),
                    etGeoapifyKey.getText().toString(),
                    etLocationIqKey.getText().toString(),
                    etOrsKey.getText().toString(),
                    etGraphHopperKey.getText().toString());
            KeyStorage.loadIntoConfig(requireContext());
            Toast.makeText(getContext(), "Đã lưu keys (khởi tạo MapServiceFacade mới ở lần dùng tiếp).", Toast.LENGTH_SHORT).show();
        });
    }

    @Override public void onResume(){ super.onResume(); mapProvider.onResume(); }
    @Override public void onPause(){ super.onPause(); mapProvider.onPause(); }
    @Override public void onDestroyView(){ super.onDestroyView(); mapProvider.onDestroy(); }
}