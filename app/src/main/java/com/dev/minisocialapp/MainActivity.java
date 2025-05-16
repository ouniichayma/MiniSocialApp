package com.dev.minisocialapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.dev.minisocialapp.activities.AddPostActivity;
import com.dev.minisocialapp.adapters.PostsAdapter;
import com.dev.minisocialapp.models.Post;
import com.dev.minisocialapp.models.React;
import com.dev.minisocialapp.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private RecyclerView postsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    private DatabaseReference postsRef, usersRef,reactRef;
    private FirebaseAuth auth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton menuButton = findViewById(R.id.menu_button);








        





        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        usersRef = FirebaseDatabase.getInstance().getReference("users");





        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d("AUTH", "Utilisateur connecté: " + currentUser.getUid());
        } else {
            Log.e("AUTH", "Aucun utilisateur connecté !");
        }

        // Initialisation des vues
        postsRecyclerView = findViewById(R.id.posts_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        FloatingActionButton fab = findViewById(R.id.add_post_fab);
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView username = findViewById(R.id.username);

        // Configurer le RecyclerView
        postAdapter = new PostsAdapter(postList, userList, auth.getCurrentUser().getUid(), this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(postAdapter);

        // Charger les données
        loadCurrentUser(profileImage, username);
        loadPosts();

        // Configurer le swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadPosts();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Bouton pour ajouter un post
        fab.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddPostActivity.class));
        });





    }

    private void loadCurrentUser(ImageView profileImage, TextView username) {
        String currentUserId = auth.getCurrentUser().getUid();
        Log.d("USER_LOAD", "Chargement des infos de l'utilisateur avec l'ID: " + currentUserId);
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("USER_LOAD", "Snapshot exists: " + snapshot.exists());
                User currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    Log.d("USER_LOAD", "Nom: " + currentUser.getFullName() + ", image: " + currentUser.getProfileImageUrl());
                    username.setText(currentUser.getFullName());
                    if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(currentUser.getProfileImageUrl())
                                .into(profileImage);
                    } else {
                        Log.w("USER_IMAGE", "Aucune image trouvée pour l'utilisateur.");
                       
                    }

                }else {
                    Log.e("USER_LOAD", "Utilisateur null dans le snapshot !");
                }
            }







                @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erreur de chargement du profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPosts() {
        postsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                Set<String> userIds = new HashSet<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                        userIds.add(post.getUserId());
                    }
                }

                // Charger les utilisateurs correspondants
                loadUsers(userIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erreur lors du chargement des posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers(Set<String> userIds) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getUid() != null) {
                        Log.d("UserLoaded", "User: " + user.getUid() + " - " + user.getFullName());
                        if (userIds.contains(user.getUid())) {
                            userList.add(user);
                        }
                    }

                }

                // Inverser la liste des posts (posts récents en haut)
                Collections.reverse(postList);

                // Notifier l'adapter
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erreur lors du chargement des utilisateurs", Toast.LENGTH_SHORT).show();
            }
        });
    }


















}