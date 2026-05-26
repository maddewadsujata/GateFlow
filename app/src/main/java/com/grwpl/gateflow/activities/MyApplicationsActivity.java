package com.grwpl.gateflow.activities;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.adapters.MyApplicationsAdapter;
import com.grwpl.gateflow.models.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * My Applications Activity
 * Shows all applications submitted by the student
 */
public class MyApplicationsActivity extends AppCompatActivity {

    private RecyclerView applicationsRecyclerView;
    private ProgressBar progressBar;
    private List<Application> applicationList = new ArrayList<>();
    private MyApplicationsAdapter myApplicationsAdapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        applicationsRecyclerView = findViewById(R.id.applicationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myApplicationsAdapter = new MyApplicationsAdapter(applicationList, this);
        applicationsRecyclerView.setAdapter(myApplicationsAdapter);

        // Load student applications
        loadStudentApplications();
    }

    /**
     * Load applications for current student
     */
    private void loadStudentApplications() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        String studentId = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("applications")
                .orderByChild("studentId")
                .equalTo(studentId)
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
                        myApplicationsAdapter.notifyDataSetChanged();
                    }
                });
    }
}
