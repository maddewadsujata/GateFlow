package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.models.Application;

/**
 * Application Details Activity
 * Shows detailed information about an application
 */
public class ApplicationDetailsActivity extends AppCompatActivity {

    private ImageView studentPhotoImageView, proofImageView, qrCodeImageView;
    private TextView studentNameText, rollNumberText, addressText, phoneText, parentPhoneText, reasonText, goingDateText, returnDateText, goingTimeText, returnTimeText, numberOfDaysText, passTypeText, statusText, remarksText;
    private Button approveBtn, rejectBtn, forwardBtn;
    private ProgressBar progressBar;
    private String applicationId;
    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_details);

        // Initialize views
        initializeViews();

        // Get application ID from intent
        applicationId = getIntent().getStringExtra("applicationId");

        // Load application details
        loadApplicationDetails();

        // Button listeners
        approveBtn.setOnClickListener(v -> approveApplication());
        rejectBtn.setOnClickListener(v -> showRejectDialog());
        forwardBtn.setOnClickListener(v -> showForwardDialog());
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        studentPhotoImageView = findViewById(R.id.studentPhotoImageView);
        proofImageView = findViewById(R.id.proofImageView);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        studentNameText = findViewById(R.id.studentNameText);
        rollNumberText = findViewById(R.id.rollNumberText);
        addressText = findViewById(R.id.addressText);
        phoneText = findViewById(R.id.phoneText);
        parentPhoneText = findViewById(R.id.parentPhoneText);
        reasonText = findViewById(R.id.reasonText);
        goingDateText = findViewById(R.id.goingDateText);
        returnDateText = findViewById(R.id.returnDateText);
        goingTimeText = findViewById(R.id.goingTimeText);
        returnTimeText = findViewById(R.id.returnTimeText);
        numberOfDaysText = findViewById(R.id.numberOfDaysText);
        passTypeText = findViewById(R.id.passTypeText);
        statusText = findViewById(R.id.statusText);
        remarksText = findViewById(R.id.remarksText);
        approveBtn = findViewById(R.id.approveBtn);
        rejectBtn = findViewById(R.id.rejectBtn);
        forwardBtn = findViewById(R.id.forwardBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Load application details from Firebase
     */
    private void loadApplicationDetails() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference()
                .child("applications")
                .child(applicationId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        application = task.getResult().getValue(Application.class);
                        displayApplicationDetails();
                    }
                });
    }

    /**
     * Display application details on UI
     */
    private void displayApplicationDetails() {
        if (application == null) return;

        // Load images
        Glide.with(this).load(application.getStudentPhoto()).into(studentPhotoImageView);
        if (!application.getProofImageUrl().isEmpty()) {
            Glide.with(this).load(application.getProofImageUrl()).into(proofImageView);
        }
        if (!application.getQrCode().isEmpty()) {
            Glide.with(this).load(application.getQrCode()).into(qrCodeImageView);
        }

        // Set text fields
        studentNameText.setText(application.getStudentName());
        rollNumberText.setText(application.getRollNumber());
        addressText.setText(application.getAddress());
        phoneText.setText(application.getPhoneNumber());
        parentPhoneText.setText(application.getParentPhone());
        reasonText.setText(application.getReason());
        goingTimeText.setText(application.getGoingTime());
        returnTimeText.setText(application.getReturnTime());
        numberOfDaysText.setText(String.valueOf(application.getNumberOfDays()));
        passTypeText.setText(application.getPassType());
        statusText.setText(application.getStatus());
        remarksText.setText(application.getRemarks());

        // Set status color
        updateStatusColor();
    }

    /**
     * Update status text color based on status
     */
    private void updateStatusColor() {
        String status = application.getStatus();
        if (status.equals("approved")) {
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (status.equals("rejected")) {
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    /**
     * Approve application
     */
    private void approveApplication() {
        application.setStatus("approved");
        application.setApprovedAt(System.currentTimeMillis());
        updateApplicationStatus();
    }

    /**
     * Show reject dialog
     */
    private void showRejectDialog() {
        // Would show a dialog for entering rejection remarks
    }

    /**
     * Show forward dialog
     */
    private void showForwardDialog() {
        // Would show a dialog for selecting next approver (HOD/Principal)
    }

    /**
     * Update application status in Firebase
     */
    private void updateApplicationStatus() {
        FirebaseDatabase.getInstance().getReference()
                .child("applications")
                .child(applicationId)
                .setValue(application)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendNotificationToStudent();
                        finish();
                    }
                });
    }

    /**
     * Send notification to student
     */
    private void sendNotificationToStudent() {
        // Send FCM notification to student
    }
}
