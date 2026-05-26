package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;

/**
 * Login Activity for both students and staff
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private TextView signupLink, staffLoginLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signupLink);
        staffLoginLink = findViewById(R.id.staffLoginLink);
        progressBar = findViewById(R.id.progressBar);

        // Login button click listener
        loginBtn.setOnClickListener(v -> performLogin());

        // Signup link click listener
        signupLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, StudentSignupActivity.class)));

        // Staff login link
        staffLoginLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, StaffLoginActivity.class)));
    }

    /**
     * Perform login operation
     */
    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        // Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        // Login successful
                        String userId = mAuth.getCurrentUser().getUid();
                        getUserRoleAndNavigate(userId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Get user role from database and navigate accordingly
     */
    private void getUserRoleAndNavigate(String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String role = task.getResult().child("role").getValue(String.class);
                        saveUserPreferences(userId, role);

                        if ("student".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        } else if ("watchman".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, WatchmanDashboardActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, StaffDashboardActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Save user preferences locally
     */
    private void saveUserPreferences(String userId, String role) {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .putString("userId", userId)
                .putString("role", role)
                .apply();
    }
}
