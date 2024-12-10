package com.example.projectwmp;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectwmp.helper.PeriodLogHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class periodLogActivity extends AppCompatActivity {


    private String startDate, endDate; // Declare these as instance variables
//    private EditText usernameEditText;
    private EditText startDateEditText, endDateEditText;
    private Button submitButton, display;
    ConstraintLayout back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_period_log);

//        usernameEditText = findViewById(R.id.username_input);
        startDateEditText = findViewById(R.id.start_date_picker);
        endDateEditText = findViewById(R.id.end_date_picker);
        submitButton = findViewById(R.id.submit_button);
        display  = findViewById(R.id.displayyy);
        back = findViewById(R.id.backB);


        // Retrieve the logged-in username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUsername = sharedPreferences.getString("username", null);


        // Log the value of the retrieved username for debugging
        Log.d(TAG, "Logged-in username: " + loggedInUsername);



//        if (loggedInUsername != null) {
//            usernameEditText.setText(loggedInUsername);
//        } else {
//            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show();
//            finish(); // Close activity if no user is logged in
//        }

        startDateEditText.setOnClickListener(v -> pickDate(startDateEditText, true));
        endDateEditText.setOnClickListener(v -> pickDate(endDateEditText, false));


        submitButton.setOnClickListener(v -> {
            if (loggedInUsername != null) {
                if (validateDates()) {
                    // Proceed to check if the date exists in Firebase
                    storePeriodLogToFirebase(loggedInUsername, startDate, endDate);
                }
            } else {
                Toast.makeText(periodLogActivity.this, "No user logged in!", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(periodLogActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        display.setOnClickListener(v -> {
            Intent intent = new Intent(periodLogActivity.this, PeriodLogDisplayActivity.class);
            startActivity(intent);
        });

    }

    private void pickDate(EditText editText, boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    editText.setText(selectedDate);

                    // Store the selected date into the correct variable
                    if (isStartDate) {
                        startDate = selectedDate;
                    } else {
                        endDate = selectedDate;
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }


    private void storePeriodLogToFirebase(String username, String startDate, String endDate) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users").child(username).child("periodLogs");

        // Query for overlapping or identical dates
        reference.orderByChild("startDate").equalTo(startDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean dateExists = false;

                        for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                            PeriodLogHelper existingLog = logSnapshot.getValue(PeriodLogHelper.class);

                            if (existingLog != null && existingLog.getEndDate().equals(endDate)) {
                                dateExists = true;
                                break;
                            }
                        }

                        if (dateExists) {
                            Toast.makeText(periodLogActivity.this, "A log with these dates already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            saveNewPeriodLog(reference, username, startDate, endDate);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(periodLogActivity.this, "Error checking existing logs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNewPeriodLog(DatabaseReference reference, String username, String startDate, String endDate) {
        // Create a unique key for the new period log
        String logId = reference.push().getKey();

        // Create a PeriodLogHelper object
        PeriodLogHelper periodLog = new PeriodLogHelper(startDate, endDate);

        // Save the new log in Firebase
        reference.child(logId).setValue(periodLog)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(periodLogActivity.this, "Period log saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(periodLogActivity.this, "Failed to save period log: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateDates() {
        if (startDate == null || startDate.isEmpty()) {
            Toast.makeText(this, "Start Date is required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endDate == null || endDate.isEmpty()) {
            Toast.makeText(this, "End Date is required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            // Parse the dates
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false); // Strict parsing

            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Date today = new Date();

            // Check if start date is after end date
            if (start != null && end != null && start.after(end)) {
                Toast.makeText(this, "Start Date cannot be after End Date!", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Check if dates are in the future
            if (start != null && start.after(today)) {
                Toast.makeText(this, "Start Date cannot be in the future!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (end != null && end.after(today)) {
                Toast.makeText(this, "End Date cannot be in the future!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}
