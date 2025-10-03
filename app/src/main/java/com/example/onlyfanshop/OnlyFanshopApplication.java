package com.example.onlyfanshop;

import android.app.Application;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class OnlyFanshopApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}

