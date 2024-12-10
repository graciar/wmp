package com.example.projectwmp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signupActivity extends AppCompatActivity {


    EditText userSignUp, emailSignup, passSignup;
    Button signupBtn;
    TextView redirectText;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);


        userSignUp =findViewById(R.id.user_signup);
        emailSignup =findViewById(R.id.email_signup);
        passSignup =findViewById(R.id.pass_signup);
        signupBtn =findViewById(R.id.signup_Btn);
        redirectText =findViewById(R.id.direct_text);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize Firebase database instance
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                // Debug: Confirm Firebase reference creation
                Log.d(TAG, "Firebase reference obtained: " + reference);

                // Retrieve user input
                String username = userSignUp.getText().toString();
                String email = emailSignup.getText().toString();
                String password = passSignup.getText().toString();

                // Debug: Log retrieved inputs
                Log.d(TAG, "Inputs retrieved. Username: " + username + ", Email: " + email);


                HelperClass helperClass = new HelperClass(email, username, password);

                // Debug: Confirm HelperClass object creation
                Log.d(TAG, "Creating HelperClass object: " + helperClass);

                // Write data to Firebase
                reference.child(username).setValue(helperClass)
                        .addOnSuccessListener(aVoid -> {
                            // Debug: Log successful write
                            Log.d(TAG, "Data successfully written to Firebase");

                            Toast.makeText(signupActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();

                            // Redirect to login activity
                            Intent intent = new Intent(signupActivity.this, loginActivity.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(error -> {
                            // Debug: Log error message
                            Log.e(TAG, "Error writing to Firebase: " + error.getMessage());
                        });
            }
        });

        redirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signupActivity.this, loginActivity.class);
                startActivity(intent);
            }
        });
    }
}
