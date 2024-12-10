package com.example.projectwmp.adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectwmp.R;
import com.example.projectwmp.helper.PeriodLogHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PeriodLogAdapter extends RecyclerView.Adapter<PeriodLogAdapter.ViewHolder> {

    private final List<PeriodLogHelper> periodLogs;

    public PeriodLogAdapter(List<PeriodLogHelper> periodLogs) {
        this.periodLogs = periodLogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_period_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PeriodLogHelper periodLog = periodLogs.get(position);

        holder.startDate.setText(periodLog.getStartDate());
        holder.endDate.setText(periodLog.getEndDate());

        holder.deleteButton.setOnClickListener(v -> {
            String username = holder.itemView.getContext()
                    .getSharedPreferences("UserPrefs", 0)
                    .getString("username", null);

            if (username != null) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users").child(username).child("periodLogs").child(periodLog.getLogId());

                ref.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            int currentPosition = holder.getAdapterPosition(); // Get the current adapter position
                            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < periodLogs.size()) {
                                periodLogs.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                                Log.d(TAG, "Successfully deleted log at position: " + currentPosition);
                                Toast.makeText(holder.itemView.getContext(), "Log deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Invalid adapter position: " + currentPosition);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to delete log: " + periodLog.getLogId(), e);
                            Toast.makeText(holder.itemView.getContext(), "Failed to delete log", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e(TAG, "Username is null. Cannot delete log.");
                Toast.makeText(holder.itemView.getContext(), "Failed to delete log. User not logged in.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return periodLogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView startDate, endDate;
        public Button deleteButton;

        public ViewHolder(View view) {
            super(view);
            startDate = view.findViewById(R.id.start_date);
            endDate = view.findViewById(R.id.end_date);
            deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
