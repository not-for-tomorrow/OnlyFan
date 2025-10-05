package com.example.onlyfan.Activity;

import com.example.onlyfan.ui.CategoryFragment;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.onlyfan.R;
import com.example.onlyfan.ui.HomeFragment;
import com.example.onlyfan.ui.ProfileFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.FirebaseDatabase;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends AppCompatActivity {

    private ChipNavigationBar bottomNav;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        initFirebaseTest();
        initNavigation(savedInstanceState);
        initSearchActions();
    }

    private void initFirebaseTest() {
        FirebaseDatabase.getInstance().getReference("test_connection")
                .setValue("Hello Firebase!")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FIREBASE", "✅ Kết nối Firebase thành công!");
                    } else {
                        Log.e("FIREBASE", "❌ Lỗi kết nối Firebase", task.getException());
                    }
                });
    }

    private void initNavigation(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            bottomNav.setItemSelected(R.id.nav_home, true);
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(id -> {
            Fragment fragment;
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_category) {
                fragment = new CategoryFragment();
            } else if (id == R.id.nav_fav) {
                fragment = PlaceholderFragment.newInstance("Favorites");
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else {
                fragment = new HomeFragment();
            }
            loadFragment(fragment);
        });
    }

    private void initSearchActions() {
        // Bạn có thể gắn sự kiện search ở đây nếu muốn chuyển vào HomeFragment thông qua interface
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }
}