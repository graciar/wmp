package com.example.projectwmp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private ImageView imageView;
    private Button btnSnap, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageView = findViewById(R.id.imageview1);
        btnSnap = findViewById(R.id.btncamera);
        logout = findViewById(R.id.logoutBtn);


        loadProfilePicture();

        btnSnap.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_CODE);
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ProfileActivity.this, loginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap picTaken = (Bitmap) data.getExtras().get("data");

            if (picTaken != null) {
                // Save image
                String savedImagePath = saveImageToInternalStorage(picTaken);
                if (savedImagePath != null) {
                    saveProfilePicturePath(savedImagePath);
                    imageView.setImageBitmap(picTaken);
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Camera Canceled", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Save image to internal storage
    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "profile_picture.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Save the image path in SharedPreferences
    private void saveProfilePicturePath(String path) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profilePicturePath", path);
        editor.apply();
    }

    // Load the profile picture from SharedPreferences
    private void loadProfilePicture() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String imagePath = sharedPreferences.getString("profilePicturePath", null);

        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
            } else {
                Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
