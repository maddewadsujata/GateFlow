package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.models.User;

/**
 * Student Signup Activity
 * Handles student registration with photo upload
 */
public class StudentSignupActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput, phoneInput, rollNumberInput, addressInput, parentPhoneInput;
    private Spinner departmentSpinner;
    private ImageView photoImageView;
    private Button photoBtn, signupBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Uri selectedPhotoUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Photo picker
        photoBtn.setOnClickListener(v -> pickPhoto());

        // Signup button
        signupBtn.setOnClickListener(v -> performSignup());
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        phoneInput = findViewById(R.id.phoneInput);
        rollNumberInput = findViewById(R.id.rollNumberInput);
        addressInput = findViewById(R.id.addressInput);
        parentPhoneInput = findViewById(R.id.parentPhoneInput);
        departmentSpinner = findViewById(R.id.departmentSpinner);
        photoImageView = findViewById(R.id.photoImageView);
        photoBtn = findViewById(R.id.photoBtn);
        signupBtn = findViewById(R.id.signupBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Pick photo from gallery
     */
    private void pickPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            photoImageView.setImageURI(selectedPhotoUri);
        }
    }

    /**
     * Perform signup operation
     */
    private void performSignup() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String rollNumber = rollNumberInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String parentPhone = parentPhoneInput.getText().toString().trim();
        String department = departmentSpinner.getSelectedItem().toString();

        // Validate inputs
        if (!validateInputs(name, email, password, confirmPassword, phone, rollNumber, address, parentPhone)) {
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        // Create Firebase account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        uploadPhotoAndSaveUser(userId, name, email, phone, rollNumber, address, parentPhone, department);
                    } else {
                        progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(StudentSignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Upload photo and save user data
     */
    private void uploadPhotoAndSaveUser(String userId, String name, String email, String phone, String rollNumber, String address, String parentPhone, String department) {
        if (selectedPhotoUri != null) {
            FirebaseStorage.getInstance().getReference()
                    .child("student_photos/" + userId + ".jpg")
                    .putFile(selectedPhotoUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> saveUserToDatabase(userId, name, email, phone, rollNumber, address, parentPhone, department, uri.toString()))
                    )
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(StudentSignupActivity.this, "Photo upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveUserToDatabase(userId, name, email, phone, rollNumber, address, parentPhone, department, "");
        }
    }

    /**
     * Save user data to Firebase Database
     */
    private void saveUserToDatabase(String userId, String name, String email, String phone, String rollNumber, String address, String parentPhone, String department, String photoUrl) {
        User user = new User(name, email, phone, "student", department);
        user.setUserId(userId);
        user.setStudentId(rollNumber);
        user.setAddress(address);
        user.setParentPhone(parentPhone);
        user.setPhotoUrl(photoUrl);

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .setValue(user)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        saveUserPreferences(userId, "student");
                        Toast.makeText(StudentSignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(StudentSignupActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(StudentSignupActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs(String name, String email, String password, String confirmPassword, String phone, String rollNumber, String address, String parentPhone) {
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email is required");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }

        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            phoneInput.setError("Valid phone number is required");
            return false;
        }

        if (TextUtils.isEmpty(rollNumber)) {
            rollNumberInput.setError("Roll number is required");
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            addressInput.setError("Address is required");
            return false;
        }

        if (TextUtils.isEmpty(parentPhone) || parentPhone.length() < 10) {
            parentPhoneInput.setError("Valid parent phone is required");
            return false;
        }

        return true;
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
