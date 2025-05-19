package com.dev.minisocialapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dev.minisocialapp.MainActivity;
import com.dev.minisocialapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Déclaration des composants de l'interface utilisateur
    private EditText etEmail, etPassword;
    private Button btnLogin;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Liaison des composants du layout aux variables Java
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Initialisation de Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Définir le comportement du bouton lorsqu'on clique dessus
        btnLogin.setOnClickListener(v -> loginUser());
    }







// Méthode personnalisée pour afficher une Toast stylisée

    private void showCustomToast(String message, boolean success) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(android.R.id.content), false);

        // Récupère les éléments de la toast personnalisée
        TextView toastText = layout.findViewById(R.id.toast_message);
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);

        toastText.setText(message);

        // Selon le succès ou l’échec, on change l’icône et le fond
        if (success) {
            toastIcon.setImageResource(R.drawable.ic_check_circle); // Icône de succès
            layout.setBackgroundResource(R.drawable.toast_background);
        } else {
            toastIcon.setImageResource(R.drawable.ic_error); // Icône d'erreur
            layout.setBackgroundResource(R.drawable.toast_background); // Tu peux créer une autre couleur si tu veux
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }











    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showCustomToast("Connexion réussie", true);
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        showCustomToast("Erreur : " + task.getException().getMessage(), false);
                    }
                });
    }
}