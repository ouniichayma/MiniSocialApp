package com.dev.minisocialapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.minisocialapp.activities.AddPostActivity;
import com.dev.minisocialapp.adapters.PostsAdapter;
import com.dev.minisocialapp.models.Post;
import com.dev.minisocialapp.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PostsAdapter.OnPostInteractionListener {

    private RecyclerView postsRecyclerView;
    private PostsAdapter adapter;
    private List<Post> postsList = new ArrayList<>();
    private Map<String, User> usersMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ou ton layout principal

        postsRecyclerView = findViewById(R.id.posts_recycler_view);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        adapter = new PostsAdapter(postsList, usersMap, this);
        postsRecyclerView.setAdapter(adapter);



        FloatingActionButton fab = findViewById(R.id.add_post_fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
            startActivity(intent);
        });
    }





    @Override
    public void onLikeClicked(Post post) {
        // Logique pour aimer le post (ex: update Firebase)
        Toast.makeText(this, "Liked post: " + post.getText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDislikeClicked(Post post) {
        Toast.makeText(this, "Disliked post: " + post.getText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShowCommentsClicked(Post post) {
        Toast.makeText(this, "Afficher commentaires du post: " + post.getText(), Toast.LENGTH_SHORT).show();
        // Gérer l’affichage / cache du RecyclerView commentaires dans l’adapter via notifyItemChanged
    }

    @Override
    public void onSendComment(Post post, String commentText) {
        Toast.makeText(this, "Commentaire envoyé: " + commentText, Toast.LENGTH_SHORT).show();
        // Envoyer le commentaire sur Firebase / serveur
    }
}