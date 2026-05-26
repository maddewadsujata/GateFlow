package com.grwpl.gateflow.activities;

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
 * Edit Profile Activity
 * Allows user to edit their profile information
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView photoImageView;
    private EditText nameInput, phoneInput, addressInput;
    private Button changePhotoBtn, saveBtn, cancelBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Uri selectedPhotoUri;
    private User currentUser;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        photoImageView = findViewById(R.id.photoImageView);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        changePhotoBtn = findViewById(R.id.changePhotoBtn);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        progressBar = findViewById(R.id.progressBar);

        // Load user profile
        loadUserProfile();

        // Button listeners
        changePhotoBtn.setOnClickListener(v -> pickPhoto());
        saveBtn.setOnClickListener(v -> saveChanges());
        cancelBtn.setOnClickListener(v -> finish());
    }

    /**
     * Load user profile
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
     * Display profile
     */
    private void displayProfile() {
        if (currentUser == null) return;

        Glide.with(this).load(currentUser.getPhotoUrl()).into(photoImageView);
        nameInput.setText(currentUser.getName());
        phoneInput.setText(currentUser.getPhone());
        addressInput.setText(currentUser.getAddress());
    }

    /**
     * Pick photo
     */
    private void pickPhoto() {
        android.content.Intent intent = new android.content.Intent();
        intent.setType("image/*");
        intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
        startActivityForResult(android.content.Intent.createChooser(intent, "Select Photo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            photoImageView.setImageURI(selectedPhotoUri);
        }
    }

    /**
     * Save changes to profile
     */
    private void saveChanges() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone is required");
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);

        // Upload new photo if selected
        if (selectedPhotoUri != null) {
            uploadPhotoAndSave();
        } else {
            saveUserProfile();
        }
    }

    /**
     * Upload photo and save
     */
    private void uploadPhotoAndSave() {
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseStorage.getInstance().getReference()
                .child("student_photos/" + userId + ".jpg")
                .putFile(selectedPhotoUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            currentUser.setPhotoUrl(uri.toString());
                            saveUserProfile();
                        })
                )
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(EditProfileActivity.this, "Photo upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Save user profile to database
     */
    private void saveUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .setValue(currentUser)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
