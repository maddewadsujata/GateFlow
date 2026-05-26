package com.grwpl.gateflow;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Custom Application class for GateFlow
 * Handles Firebase initialization and app-wide configuration
 */
public class MyApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Enable Firebase persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Set default theme mode (Light mode by default)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Get application context
     */
    public static Context getAppContext() {
        return appContext;
    }
}
