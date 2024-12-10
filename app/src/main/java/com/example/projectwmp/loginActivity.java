package com.example.projectwmp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    private static final String TAG = "loginActivity";
    EditText loginUser, loginPass;
    Button loginBtn;
    TextView signupRedirect;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Check if the user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false); // Define isLoggedIn here

        if (isLoggedIn) {
            // Redirect to MainActivity
            Intent intent = new Intent(loginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Close the LoginActivity
            return;  // Exit the onCreate method
        }

        // Initialize UI components
        loginUser = findViewById(R.id.user_login);
        loginPass = findViewById(R.id.pass_login);
        loginBtn = findViewById(R.id.login_Btn);
        signupRedirect = findViewById(R.id.signup_redirect);

        // Set up button listeners
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() | !validatePassword()) {
                    Log.d(TAG, "Validation failed. Username or password is empty.");
                } else {
                    checkUser();
                }
            }
        });

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, signupActivity.class);
                startActivity(intent);
            }
        });
    }



    public Boolean validateUsername() {
        String val = loginUser.getText().toString();

        if (val.isEmpty()) {
            loginUser.setError("Username cannot be empty");
            return false;
        } else {
            loginUser.setError(null);
            return true;
        }
    }

    public boolean validatePassword() {
        String val = loginPass.getText().toString();

        if (val.isEmpty()) {
            loginPass.setError("Password cannot be empty");
            return false;
        } else {
            loginPass.setError(null);
            return true;
        }
    }

    // Check user in Firebase
    public void checkUser() {
        String userUsername = loginUser.getText().toString().trim();
        String userPass = loginPass.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        Log.d(TAG, "Executing query for username: " + userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange triggered.");

                if (snapshot.exists()) {
                    Log.d(TAG, "User found in database.");
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String passwordFromDB = userSnapshot.child("password").getValue(String.class);
                        Log.d(TAG, "Password from DB: " + passwordFromDB);


                        if (passwordFromDB != null && passwordFromDB.equals(userPass)) {
                            Log.d(TAG, "Password matches. Login successful.");
                            loginUser.setError(null);


                            // Store login state in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", userUsername);  // Store the username
                            editor.putBoolean("isLoggedIn", true);  // Save the login state
                            editor.apply();  // Save the changes


                            Intent intent = new Intent(loginActivity.this, MainActivity.class);
                            startActivity(intent);
                            return;
                        }
                    }
                    Log.d(TAG, "Password mismatch. Login failed.");
                    loginPass.setError("Invalid credentials");
                    loginPass.requestFocus();
                } else {
                    Log.d(TAG, "User does not exist in database.");
                    loginUser.setError("Username does not exist.");
                    loginUser.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(loginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
