package com.grwpl.gateflow.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.models.Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Application Form Activity
 * Students fill and submit GatePass or GaVPass application
 */
public class ApplicationFormActivity extends AppCompatActivity {

    private EditText nameInput, rollNumberInput, addressInput, phoneInput, parentPhoneInput, reasonInput, goingTimeInput, returnTimeInput;
    private Spinner departmentSpinner;
    private RadioGroup passTypeRadioGroup;
    private TextView goingDateText, returnDateText, numberOfDaysText;
    private ImageView proofImageView;
    private Button selectGoingDateBtn, selectReturnDateBtn, selectProofImageBtn, submitBtn, resetBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Uri selectedProofUri;
    private long selectedGoingDate, selectedReturnDate;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int DATE_PICKER_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_form);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Button listeners
        selectGoingDateBtn.setOnClickListener(v -> pickDate("going"));
        selectReturnDateBtn.setOnClickListener(v -> pickDate("return"));
        selectProofImageBtn.setOnClickListener(v -> pickProofImage());
        submitBtn.setOnClickListener(v -> submitApplication());
        resetBtn.setOnClickListener(v -> resetForm());
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        rollNumberInput = findViewById(R.id.rollNumberInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        parentPhoneInput = findViewById(R.id.parentPhoneInput);
        reasonInput = findViewById(R.id.reasonInput);
        goingTimeInput = findViewById(R.id.goingTimeInput);
        returnTimeInput = findViewById(R.id.returnTimeInput);
        departmentSpinner = findViewById(R.id.departmentSpinner);
        passTypeRadioGroup = findViewById(R.id.passTypeRadioGroup);
        goingDateText = findViewById(R.id.goingDateText);
        returnDateText = findViewById(R.id.returnDateText);
        numberOfDaysText = findViewById(R.id.numberOfDaysText);
        proofImageView = findViewById(R.id.proofImageView);
        selectGoingDateBtn = findViewById(R.id.selectGoingDateBtn);
        selectReturnDateBtn = findViewById(R.id.selectReturnDateBtn);
        selectProofImageBtn = findViewById(R.id.selectProofImageBtn);
        submitBtn = findViewById(R.id.submitBtn);
        resetBtn = findViewById(R.id.resetBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Pick date from date picker
     */
    private void pickDate(String dateType) {
        // This would integrate with a DatePickerDialog
        // For now, we'll use a simple date picker
        android.app.DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            Date date = new Date(year - 1900, month, dayOfMonth);
            long timestamp = date.getTime();

            if (dateType.equals("going")) {
                selectedGoingDate = timestamp;
                goingDateText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date));
            } else {
                selectedReturnDate = timestamp;
                returnDateText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date));
                calculateNumberOfDays();
            }
        };

        Date today = new Date();
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(this, listener, today.getYear() + 1900, today.getMonth(), today.getDate());
        dialog.show();
    }

    /**
     * Calculate number of days between dates
     */
    private void calculateNumberOfDays() {
        if (selectedGoingDate > 0 && selectedReturnDate > 0) {
            long daysDifference = (selectedReturnDate - selectedGoingDate) / (24 * 60 * 60 * 1000);
            numberOfDaysText.setText(String.valueOf(daysDifference + 1));
        }
    }

    /**
     * Pick proof image from gallery
     */
    private void pickProofImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Proof Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedProofUri = data.getData();
            proofImageView.setImageURI(selectedProofUri);
        }
    }

    /**
     * Submit application
     */
    private void submitApplication() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);

        String name = nameInput.getText().toString().trim();
        String rollNumber = rollNumberInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String parentPhone = parentPhoneInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String goingTime = goingTimeInput.getText().toString().trim();
        String returnTime = returnTimeInput.getText().toString().trim();
        String passType = ((RadioButton) findViewById(passTypeRadioGroup.getCheckedRadioButtonId())).getText().toString();
        String department = departmentSpinner.getSelectedItem().toString();
        String studentId = mAuth.getCurrentUser().getUid();
        int numberOfDays = Integer.parseInt(numberOfDaysText.getText().toString());

        // Upload proof image if selected
        if (selectedProofUri != null) {
            FirebaseStorage.getInstance().getReference()
                    .child("proof_images/" + studentId + "_" + System.currentTimeMillis() + ".jpg")
                    .putFile(selectedProofUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> saveApplication(name, rollNumber, address, phone, parentPhone, reason, goingTime, returnTime, passType, department, studentId, numberOfDays, uri.toString()))
                    )
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(ApplicationFormActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveApplication(name, rollNumber, address, phone, parentPhone, reason, goingTime, returnTime, passType, department, studentId, numberOfDays, "");
        }
    }

    /**
     * Save application to Firebase
     */
    private void saveApplication(String name, String rollNumber, String address, String phone, String parentPhone, String reason, String goingTime, String returnTime, String passType, String department, String studentId, int numberOfDays, String proofImageUrl) {
        Application application = new Application();
        application.setApplicationId(FirebaseDatabase.getInstance().getReference("applications").push().getKey());
        application.setStudentId(studentId);
        application.setStudentName(name);
        application.setRollNumber(rollNumber);
        application.setAddress(address);
        application.setPhoneNumber(phone);
        application.setParentPhone(parentPhone);
        application.setReason(reason);
        application.setGoingTime(goingTime);
        application.setReturnTime(returnTime);
        application.setPassType(passType);
        application.setDepartment(department);
        application.setNumberOfDays(numberOfDays);
        application.setProofImageUrl(proofImageUrl);
        application.setStatus("pending");
        application.setAppliedAt(System.currentTimeMillis());
        application.setGoingDate(selectedGoingDate);
        application.setReturnDate(selectedReturnDate);

        FirebaseDatabase.getInstance().getReference()
                .child("applications")
                .child(application.getApplicationId())
                .setValue(application)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(ApplicationFormActivity.this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ApplicationFormActivity.this, "Failed to submit application", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        String name = nameInput.getText().toString().trim();
        String rollNumber = rollNumberInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String parentPhone = parentPhoneInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String goingTime = goingTimeInput.getText().toString().trim();
        String returnTime = returnTimeInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
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

        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            phoneInput.setError("Valid phone is required");
            return false;
        }

        if (TextUtils.isEmpty(parentPhone) || parentPhone.length() < 10) {
            parentPhoneInput.setError("Valid parent phone is required");
            return false;
        }

        if (TextUtils.isEmpty(reason)) {
            reasonInput.setError("Reason is required");
            return false;
        }

        if (TextUtils.isEmpty(goingTime)) {
            goingTimeInput.setError("Going time is required");
            return false;
        }

        if (TextUtils.isEmpty(returnTime)) {
            returnTimeInput.setError("Return time is required");
            return false;
        }

        if (selectedGoingDate == 0 || selectedReturnDate == 0) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Reset form to default state
     */
    private void resetForm() {
        nameInput.setText("");
        rollNumberInput.setText("");
        addressInput.setText("");
        phoneInput.setText("");
        parentPhoneInput.setText("");
        reasonInput.setText("");
        goingTimeInput.setText("");
        returnTimeInput.setText("");
        goingDateText.setText("");
        returnDateText.setText("");
        numberOfDaysText.setText("0");
        proofImageView.setImageBitmap(null);
        selectedGoingDate = 0;
        selectedReturnDate = 0;
        selectedProofUri = null;
        passTypeRadioGroup.clearCheck();
    }
}
