package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.grwpl.gateflow.R;

/**
 * Splash Screen Activity
 * Shows welcome screen and handles auto-login
 */
public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final long SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        // Delay and navigate to appropriate screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserStatus, SPLASH_DURATION);
    }

    /**
     * Check if user is already logged in
     */
    private void checkUserStatus() {
        if (mAuth.getCurrentUser() != null) {
            // User is logged in, go to appropriate dashboard
            navigateToDashboard();
        } else {
            // No user logged in, show login screen
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }

    /**
     * Navigate to appropriate dashboard based on user role
     */
    private void navigateToDashboard() {
        // Get user role from SharedPreferences or Firebase
        String userRole = getSharedPreferences("user_pref", MODE_PRIVATE).getString("role", "student");

        if (userRole.equals("student")) {
            startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
        } else if (userRole.equals("watchman")) {
            startActivity(new Intent(SplashActivity.this, WatchmanDashboardActivity.class));
        } else {
            // staff (warden, hod, principal)
            startActivity(new Intent(SplashActivity.this, StaffDashboardActivity.class));
        }
        finish();
    }
}
