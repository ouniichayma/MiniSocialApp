package com.dev.minisocialapp.activities;

import android.app.DatePickerDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.cloudinary.android.MediaManager;

import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.User;

import com.dev.minisocialapp.utils.CloudinaryUtils;
import com.dev.minisocialapp.utils.FileUtils;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etFullName, etEmail, etDob, etPhone, etPassword;
    private ImageView imageProfile;
    private Button btnRegister;
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etDob = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        imageProfile = findViewById(R.id.imageProfile);
        btnRegister = findViewById(R.id.btnRegister);

        imageProfile.setOnClickListener(v -> pickImageFromGallery());

        findViewById(R.id.btnAddPhoto).setOnClickListener(v -> pickImageFromGallery());

        etDob.setOnClickListener(v -> showDatePicker());

        btnRegister.setOnClickListener(v -> registerUser());

        // Init Cloudinary
        MediaManager.init(this, CloudinaryUtils.getInstance().config);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageProfile.setImageURI(selectedImageUri);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = day + "/" + (month + 1) + "/" + year;
            etDob.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty() ) {
            Toast.makeText(this, "le champs nom est requis", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "le champs email est requis", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dob.isEmpty()) {
            Toast.makeText(this, "le champs date de naissance est requis", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "le champs telephone est requis", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "le champs image est requis", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "le champs mot de passe est requis", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        uploadImageToCloudinary(selectedImageUri, imageUrl -> {
            uploadedImageUrl = imageUrl;

            // Créer utilisateur Firebase
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();

                    User user = new User(fullName, email, dob, phone, uploadedImageUrl, uid);
                    database.getReference("users").child(uid).setValue(user).addOnCompleteListener(storeTask -> {
                        if (storeTask.isSuccessful()) {
                            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show();
                        }
                        btnRegister.setEnabled(true);
                    });
                } else {
                    Toast.makeText(this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    btnRegister.setEnabled(true);
                }
            });
        });
    }

    private void uploadImageToCloudinary(Uri uri, final OnImageUploaded callback) {
        try {
            String filePath = FileUtils.getRealPath(this, uri);
            File file = new File(filePath);
            String publicId = "profile_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            MediaManager.get().upload(file.getAbsolutePath())
                    .option("public_id", publicId)
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            callback.onUploaded(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            Toast.makeText(RegisterActivity.this, "Échec de l'upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            btnRegister.setEnabled(true);
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                    }).dispatch();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur fichier image", Toast.LENGTH_SHORT).show();
            btnRegister.setEnabled(true);
        }
    }

    interface OnImageUploaded {
        void onUploaded(String imageUrl);
    }
}