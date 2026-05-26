package com.grwpl.gateflow.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.grwpl.gateflow.R;

/**
 * Settings Activity
 * Allows user to configure app settings like theme, notifications, etc.
 */
public class SettingsActivity extends AppCompatActivity {

    private Spinner themeSpinner, notificationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        themeSpinner = findViewById(R.id.themeSpinner);
        notificationSpinner = findViewById(R.id.notificationSpinner);

        // Setup theme spinner
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this,
                R.array.theme_options, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        // Setup notification spinner
        ArrayAdapter<CharSequence> notificationAdapter = ArrayAdapter.createFromResource(this,
                R.array.notification_options, android.R.layout.simple_spinner_item);
        notificationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationSpinner.setAdapter(notificationAdapter);
    }
}
