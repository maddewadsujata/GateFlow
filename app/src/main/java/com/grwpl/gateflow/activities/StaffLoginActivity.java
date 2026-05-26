package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.models.User;

/**
 * Staff Login Activity
 * Allows staff to login and verify their credentials
 */
public class StaffLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, staffIdInput;
    private Spinner roleSpinner;
    private Button loginBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        staffIdInput = findViewById(R.id.staffIdInput);
        roleSpinner = findViewById(R.id.roleSpinner);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        // Login button click listener
        loginBtn.setOnClickListener(v -> performLogin());
    }

    /**
     * Perform staff login
     */
    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String staffId = staffIdInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        // Validate inputs
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email is required");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        if (TextUtils.isEmpty(staffId)) {
            staffIdInput.setError("Staff ID is required");
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        // Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        verifyStaffAndNavigate(userId, role, staffId);
                    } else {
                        progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(StaffLoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Verify staff credentials and navigate
     */
    private void verifyStaffAndNavigate(String userId, String role, String staffId) {
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        String userRole = task.getResult().child("role").getValue(String.class);
                        if (userRole != null && (userRole.equals("warden") || userRole.equals("hod") || userRole.equals("principal") || userRole.equals("watchman"))) {
                            saveUserPreferences(userId, userRole);
                            startActivity(new Intent(StaffLoginActivity.this, StaffDashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(StaffLoginActivity.this, "Invalid staff credentials", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(StaffLoginActivity.this, "Staff not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Save user preferences
     */
    private void saveUserPreferences(String userId, String role) {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .putString("userId", userId)
                .putString("role", role)
                .apply();
    }
}
