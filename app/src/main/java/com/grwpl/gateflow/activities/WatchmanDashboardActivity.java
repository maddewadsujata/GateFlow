package com.grwpl.gateflow.activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.grwpl.gateflow.R;
import com.grwpl.gateflow.adapters.WatchmanApplicationAdapter;
import com.grwpl.gateflow.models.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Watchman Dashboard Activity
 * Shows approved passes for watchman to verify exit/entry
 */
public class WatchmanDashboardActivity extends AppCompatActivity {

    private RecyclerView approvedPassesRecyclerView;
    private SearchView searchView;
    private ProgressBar progressBar;
    private List<Application> approvedPassesList = new ArrayList<>();
    private WatchmanApplicationAdapter watchmanApplicationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchman_dashboard);

        // Initialize views
        approvedPassesRecyclerView = findViewById(R.id.approvedPassesRecyclerView);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        approvedPassesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        watchmanApplicationAdapter = new WatchmanApplicationAdapter(approvedPassesList, this);
        approvedPassesRecyclerView.setAdapter(watchmanApplicationAdapter);

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPasses(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPasses(newText);
                return false;
            }
        });

        // Load approved passes
        loadApprovedPasses();
    }

    /**
     * Load approved passes from Firebase
     */
    private void loadApprovedPasses() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference()
                .child("applications")
                .orderByChild("status")
                .equalTo("approved")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult().exists()) {
                        approvedPassesList.clear();
                        for (com.google.firebase.database.DataSnapshot snapshot : task.getResult().getChildren()) {
                            Application application = snapshot.getValue(Application.class);
                            if (application != null) {
                                approvedPassesList.add(application);
                            }
                        }
                        watchmanApplicationAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Filter passes based on search query
     */
    private void filterPasses(String query) {
        List<Application> filteredList = new ArrayList<>();
        for (Application app : approvedPassesList) {
            if (app.getStudentName().toLowerCase().contains(query.toLowerCase()) ||
                    app.getRollNumber().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(app);
            }
        }
        watchmanApplicationAdapter.updateList(filteredList);
    }
}
