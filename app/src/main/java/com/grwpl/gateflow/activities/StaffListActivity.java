package com.grwpl.gateflow.activities;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.adapters.StaffAdapter;
import com.grwpl.gateflow.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Staff List Activity
 * Shows warden, HOD, and principal for selected department
 */
public class StaffListActivity extends AppCompatActivity {

    private RecyclerView staffRecyclerView;
    private ProgressBar progressBar;
    private List<User> staffList = new ArrayList<>();
    private StaffAdapter staffAdapter;
    private String departmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_list);

        // Get department from intent
        departmentId = getIntent().getStringExtra("departmentId");

        // Initialize views
        staffRecyclerView = findViewById(R.id.staffRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        staffRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        staffAdapter = new StaffAdapter(staffList, this);
        staffRecyclerView.setAdapter(staffAdapter);

        // Load staff
        loadStaff();
    }

    /**
     * Load staff for department
     */
    private void loadStaff() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference()
                .child("staff")
                .orderByChild("department")
                .equalTo(departmentId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        staffList.clear();
                        for (com.google.firebase.database.DataSnapshot snapshot : task.getResult().getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                staffList.add(user);
                            }
                        }
                        staffAdapter.notifyDataSetChanged();
                    }
                });
    }
}
