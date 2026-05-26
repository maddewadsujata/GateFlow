package com.grwpl.gateflow.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.adapters.DepartmentAdapter;
import com.grwpl.gateflow.models.Department;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Activity for Students
 * Shows department selection and pass application options
 */
public class DashboardActivity extends AppCompatActivity {

    private RecyclerView departmentRecyclerView;
    private ProgressBar progressBar;
    private TextView welcomeText;
    private List<Department> departmentList = new ArrayList<>();
    private DepartmentAdapter departmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        departmentRecyclerView = findViewById(R.id.departmentRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        welcomeText = findViewById(R.id.welcomeText);

        // Setup RecyclerView
        departmentRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        departmentAdapter = new DepartmentAdapter(departmentList, this);
        departmentRecyclerView.setAdapter(departmentAdapter);

        // Load departments
        loadDepartments();
    }

    /**
     * Load departments from Firebase
     */
    private void loadDepartments() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference()
                .child("departments")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        departmentList.clear();
                        for (com.google.firebase.database.DataSnapshot snapshot : task.getResult().getChildren()) {
                            Department department = snapshot.getValue(Department.class);
                            if (department != null) {
                                departmentList.add(department);
                            }
                        }
                        departmentAdapter.notifyDataSetChanged();
                    }
                });
    }
}
