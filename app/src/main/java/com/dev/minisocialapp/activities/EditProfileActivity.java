package com.dev.minisocialapp.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText editFullName, editEmail, editPhone, editDob;
    private Button buttonChooseImage, buttonSave;

    private Uri imageUri;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profile_image);
        editFullName = findViewById(R.id.edit_full_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editDob = findViewById(R.id.edit_dob);
        buttonChooseImage = findViewById(R.id.button_choose_image);
        buttonSave = findViewById(R.id.button_save);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());


        loadUserData();

        buttonChooseImage.setOnClickListener(v -> chooseImage());
        buttonSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    editFullName.setText(user.getFullName());
                    editEmail.setText(user.getEmail());
                    editPhone.setText(user.getPhone());
                    editDob.setText(user.getDateOfBirth());

                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load(user.getProfileImageUrl())
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveUserProfile() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String dob = editDob.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mise à jour...");
        progressDialog.show();

        if (imageUri != null) {
            uploadToCloudinary(imageUri, fullName, email, phone, dob, progressDialog);
        } else {
            updateUser(fullName, email, phone, dob, null);
            progressDialog.dismiss();
        }
    }


    private void uploadToCloudinary(Uri imageUri, String fullName, String email, String phone, String dob, ProgressDialog progressDialog) {
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        updateUser(fullName, email, phone, dob, imageUrl);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(EditProfileActivity.this, "Erreur Cloudinary : " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }



    private void updateUser(String fullName, String email, String phone, String dob, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("dateOfBirth", dob);

        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur de mise à jour", Toast.LENGTH_SHORT).show();
            }
        });
    }
}