package com.grwpl.gateflow.activities;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.adapters.ApplicationAdapter;
import com.grwpl.gateflow.models.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Staff Dashboard Activity
 * Shows pending applications for staff to review and approve/reject
 */
public class StaffDashboardActivity extends AppCompatActivity {

    private RecyclerView applicationsRecyclerView;
    private ProgressBar progressBar;
    private List<Application> applicationList = new ArrayList<>();
    private ApplicationAdapter applicationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        // Initialize views
        applicationsRecyclerView = findViewById(R.id.applicationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicationAdapter = new ApplicationAdapter(applicationList, this);
        applicationsRecyclerView.setAdapter(applicationAdapter);

        // Load pending applications
        loadPendingApplications();
    }

    /**
     * Load pending applications for current staff
     */
    private void loadPendingApplications() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference()
                .child("applications")
                .orderByChild("status")
                .equalTo("pending")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        applicationList.clear();
                        for (com.google.firebase.database.DataSnapshot snapshot : task.getResult().getChildren()) {
                            Application application = snapshot.getValue(Application.class);
                            if (application != null) {
                                applicationList.add(application);
                            }
                        }
                        applicationAdapter.notifyDataSetChanged();
                    }
                });
    }
}
