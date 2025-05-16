package com.dev.minisocialapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.Post;
import com.dev.minisocialapp.utils.CloudinaryUtils;
import com.dev.minisocialapp.utils.FileUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText postText;
    private ImageView selectedImage;
    private Uri imageUri;
    private Button uploadButton;
    private DatabaseReference postsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        MaterialButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());


        // Initialisation des vues
        postText = findViewById(R.id.post_text);
        selectedImage = findViewById(R.id.selected_image);
        uploadButton = findViewById(R.id.upload_post_button);
        Button selectImageButton = findViewById(R.id.select_image_button);

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        // Initialisation Cloudinary
        try {
            MediaManager.init(this, CloudinaryUtils.getInstance().config);
        } catch (IllegalStateException e) {
            // Déjà initialisé
        }

        selectImageButton.setOnClickListener(v -> openImageChooser());
        uploadButton.setOnClickListener(v -> uploadPost());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImage.setImageURI(imageUri);
            selectedImage.setVisibility(View.VISIBLE);
        }
    }

    private void uploadPost() {
        String text = postText.getText().toString().trim();

        if (text.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Veuillez ajouter du texte ou une image", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadButton.setEnabled(false);

        if (imageUri != null) {
            uploadImageToCloudinary();
        } else {
            savePostToFirebase(null);
        }
    }

    private void uploadImageToCloudinary() {
        String filePath = FileUtils.getPath(this, imageUri);
        if (filePath == null) {
            Toast.makeText(this, "Impossible de récupérer le fichier", Toast.LENGTH_SHORT).show();
            uploadButton.setEnabled(true);
            return;
        }

        MediaManager.get().upload(filePath)
                .option("folder", "social_app/posts")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        runOnUiThread(() ->
                                Toast.makeText(AddPostActivity.this, "Upload en cours...", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Peut afficher une progress bar si nécessaire
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        savePostToFirebase(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddPostActivity.this, "Échec de l'upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            uploadButton.setEnabled(true);
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Gérer la réorganisation si nécessaire
                    }
                })
                .dispatch();
    }

    private void savePostToFirebase(String imageUrl) {
        String postId = postsRef.push().getKey();
        String userId = auth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();
        String text = postText.getText().toString().trim();

        Post post = new Post(postId, userId, text, imageUrl, timestamp);

        postsRef.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddPostActivity.this, "Post publié avec succès!", Toast.LENGTH_SHORT).show();
                    finish(); // Retour à l'activité précédente
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddPostActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    uploadButton.setEnabled(true);
                });
    }
}