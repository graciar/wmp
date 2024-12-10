package com.example.projectwmp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectwmp.adapter.PeriodLogAdapter;
import com.example.projectwmp.helper.PeriodLogHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeriodLogDisplayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PeriodLogAdapter adapter;
    private List<PeriodLogHelper> periodLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period_log_display);

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        periodLogs = new ArrayList<>();
        adapter = new PeriodLogAdapter(periodLogs);
        recyclerView.setAdapter(adapter);

        // Fetch logs from Firebase
        fetchPeriodLogs();
    }

    private void fetchPeriodLogs() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUsername = sharedPreferences.getString("username", null);

        if (loggedInUsername != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("users").child(loggedInUsername).child("periodLogs");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<PeriodLogHelper> updatedPeriodLogs = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            PeriodLogHelper periodLog = snapshot.getValue(PeriodLogHelper.class);
                            if (periodLog != null) {
                                periodLog.setLogId(snapshot.getKey());
                                updatedPeriodLogs.add(periodLog);
                            }
                        } catch (Exception e) {
                            Log.e("PeriodLogDisplay", "Error parsing log: " + e.getMessage());
                        }
                    }

                    // Update adapter if data has changed
                    if (!updatedPeriodLogs.equals(periodLogs)) {
                        periodLogs.clear();
                        periodLogs.addAll(updatedPeriodLogs);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("PeriodLogDisplay", "Database error: " + databaseError.getMessage());
                    Toast.makeText(PeriodLogDisplayActivity.this, "Failed to load period logs", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in. Cannot fetch period logs.", Toast.LENGTH_SHORT).show();
        }
    }
}
