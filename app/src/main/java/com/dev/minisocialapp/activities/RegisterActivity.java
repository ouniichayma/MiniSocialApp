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

    // request code  pour choisir une image dans la galerie
    private static final int PICK_IMAGE_REQUEST = 1;
    // Déclaration des champs d’entrée utilisateur
    private EditText etFullName, etEmail, etDob, etPhone, etPassword;
    private ImageView imageProfile;
    private Button btnRegister;
    private Uri selectedImageUri;  // URI de l'image choisie
    private String uploadedImageUrl; // URL de l'image après upload vers Cloudinary

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation de Firebase Authentication et Firebase Database

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Views
        // Liaison des vues aux composants XML

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etDob = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        imageProfile = findViewById(R.id.imageProfile);
        btnRegister = findViewById(R.id.btnRegister);

        // Clic sur l'image de profil pour choisir une image

        imageProfile.setOnClickListener(v -> pickImageFromGallery());

        // Clic sur le bouton "Ajouter une photo"
        findViewById(R.id.btnAddPhoto).setOnClickListener(v -> pickImageFromGallery());

        // Clic sur le champ date pour afficher un DatePicker
        etDob.setOnClickListener(v -> showDatePicker());

        // Clic sur le bouton "S'inscrire"
        btnRegister.setOnClickListener(v -> registerUser());

        // Initialisation de Cloudinary
        MediaManager.init(this, CloudinaryUtils.getInstance().config);
    }


    // Méthode pour ouvrir la galerie d’images
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Traitement du résultat du choix d’image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageProfile.setImageURI(selectedImageUri);
        }
    }

    // Affiche une boîte de dialogue pour choisir la date de naissance
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = day + "/" + (month + 1) + "/" + year;
            etDob.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Méthode principale pour enregistrer un utilisateur
    private void registerUser() {
        // Récupération des champs
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        // Vérification des champs obligatoires
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
        // Désactiver le bouton pour éviter les doubles clics
        btnRegister.setEnabled(false);
        // Upload de l'image sur Cloudinary
        uploadImageToCloudinary(selectedImageUri, imageUrl -> {
            uploadedImageUrl = imageUrl; // Sauvegarde de l'URL de l'image

            // Création de l'utilisateur dans Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();

                    // Création d'un objet User à stocker dans Firebase Database
                    User user = new User(fullName, email, dob, phone, uploadedImageUrl, uid);

                    // Enregistrement dans la base de données
                    database.getReference("users").child(uid).setValue(user).addOnCompleteListener(storeTask -> {
                        if (storeTask.isSuccessful()) {
                            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish(); // Fermer l'activité actuelle
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


    // Méthode pour téléverser une image sur Cloudinary à partir d'une URI locale
    private void uploadImageToCloudinary(Uri uri, final OnImageUploaded callback) {
        try {
            // Convertir l'URI en chemin absolu à partir de son URI
            String filePath = FileUtils.getRealPath(this, uri); // Convertit l'URI en chemin réel
            File file = new File(filePath); // Crée un objet File à partir du chemin

            // Étape 2 : Générer un identifiant unique pour le fichier sur Cloudinary
            String publicId = "profile_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            //Lancer l'upload avec la librairie Cloudinary (MediaManager)
            MediaManager.get().upload(file.getAbsolutePath())
                    .option("public_id", publicId)
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        // Déclenché au début de l'upload (peut servir à afficher un indicateur de chargement)
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            // L'upload a réussi : on récupère l'URL de l'image
                            String imageUrl = (String) resultData.get("secure_url");
                            callback.onUploaded(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            Toast.makeText(RegisterActivity.this, "Échec de l'upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            btnRegister.setEnabled(true);  // On réactive le bouton d'inscription
                            // L'upload a échoué : on affiche un message d'erreur
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                    }).dispatch();// Lance réellement l'upload

        } catch (Exception e) {
            // Erreur lors de la récupération du fichier (ex: URI invalide ou accès refusé)
            Toast.makeText(this, "Erreur fichier image", Toast.LENGTH_SHORT).show();
            btnRegister.setEnabled(true);
        }
    }

    interface OnImageUploaded {
        void onUploaded(String imageUrl);
    }
}