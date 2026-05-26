package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.models.User;

/**
 * Profile Activity
 * Shows user profile information
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView photoImageView;
    private EditText nameInput, emailInput, phoneInput;
    private Button editBtn, logoutBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        photoImageView = findViewById(R.id.photoImageView);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        editBtn = findViewById(R.id.editBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        progressBar = findViewById(R.id.progressBar);

        // Load user profile
        loadUserProfile();

        // Edit button
        editBtn.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));

        // Logout button
        logoutBtn.setOnClickListener(v -> performLogout());
    }

    /**
     * Load user profile from Firebase
     */
    private void loadUserProfile() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        currentUser = task.getResult().getValue(User.class);
                        displayProfile();
                    }
                });
    }

    /**
     * Display profile on UI
     */
    private void displayProfile() {
        if (currentUser == null) return;

        Glide.with(this).load(currentUser.getPhotoUrl()).into(photoImageView);
        nameInput.setText(currentUser.getName());
        emailInput.setText(currentUser.getEmail());
        phoneInput.setText(currentUser.getPhone());

        // Make fields non-editable
        nameInput.setEnabled(false);
        emailInput.setEnabled(false);
        phoneInput.setEnabled(false);
    }

    /**
     * Perform logout
     */
    private void performLogout() {
        mAuth.signOut();
        getSharedPreferences("user_pref", MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }
}
