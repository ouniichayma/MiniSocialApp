package com.dev.minisocialapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.dev.minisocialapp.activities.EditProfileActivity;
import com.dev.minisocialapp.activities.LoginActivity;
import com.dev.minisocialapp.adapters.PostsAdapter;
import com.dev.minisocialapp.models.Comment;
import com.dev.minisocialapp.models.Notification;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;




import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    // Déclaration des vues et variables Firebase
    private RecyclerView postsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter postAdapter;



    // Données en mémoire
    private List<Post> postList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    private List<Notification> notificationList = new ArrayList<>();

    // Références Firebase
    private DatabaseReference postsRef, usersRef,reactRef,notificationsRef;
    private FirebaseAuth auth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Lier la vue à l'activité

        ImageButton menuButton = findViewById(R.id.menu_button);// Bouton du menu









// Gérer les actions du menu profil
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit_profile) {
                    // Ouvre l'activité ou le fragment de modification de profil
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_logout) {
                    // Déconnexion (Firebase par exemple)
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();// Afficher le menu
        });



        





        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        usersRef = FirebaseDatabase.getInstance().getReference("users");



       // Vérification de l'utilisateur connecté

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d("AUTH", "Utilisateur connecté: " + currentUser.getUid());
        } else {
            Log.e("AUTH", "Aucun utilisateur connecté !");
        }








        FloatingActionButton fab = findViewById(R.id.add_post_fab);
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView username = findViewById(R.id.username);

        // Configurer le RecyclerView
        // initialise le RecyclerView avec l’adapter personnalisé

        postsRecyclerView = findViewById(R.id.posts_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);


        postAdapter = new PostsAdapter(postList, userList, auth.getCurrentUser().getUid(), this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(postAdapter);

        // Charger les données
       // Chargement des informations de l'utilisateur courant
        loadCurrentUser(profileImage, username);
        //Chargement des publications
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



        ImageButton notificationButton = findViewById(R.id.notification_button);

        notificationButton.setOnClickListener(view -> {
            loadNotifications(); // charge les notifs puis affiche le dropdown
        });







    }


    // Charge les informations de l'utilisateur actuellement connecté (nom et image de profil)
    private void loadCurrentUser(ImageView profileImage, TextView username) {
        String currentUserId = auth.getCurrentUser().getUid();
        Log.d("USER_LOAD", "Chargement des infos de l'utilisateur avec l'ID: " + currentUserId);

        // Accède une seule fois aux données de l'utilisateur dans Firebase
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("USER_LOAD", "Snapshot exists: " + snapshot.exists());

                // Convertit le snapshot en objet User
                User currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    Log.d("USER_LOAD", "Nom: " + currentUser.getFullName() + ", image: " + currentUser.getProfileImageUrl());

                    // Affiche le nom dans l'interface
                    username.setText(currentUser.getFullName());
                    // Charge l'image si elle existe
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


    // Charge les publications (posts) de Firebase triées par date
    private void loadPosts() {
        postsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();// Vide la liste avant de la remplir
                Set<String> userIds = new HashSet<>();
                // Récupère chaque post et son auteur
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


    // Charge les utilisateurs à partir d'une liste d'IDs
    private void loadUsers(Set<String> userIds) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                // Parcourt tous les utilisateurs
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getUid() != null) {
                        Log.d("UserLoaded", "User: " + user.getUid() + " - " + user.getFullName());
                        // Ajoute l'utilisateur s'il fait partie des IDs recherchés
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









    // Charge les notifications destinées à l'utilisateur courant
    private void loadNotifications() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        notificationsRef.orderByChild("toUserId").equalTo(currentUserId)
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        List<String> userIds = new ArrayList<>();

                        // Parcourt les notifications récupérées
                        for (DataSnapshot notifSnapshot : snapshot.getChildren()) {
                            Notification notif = notifSnapshot.getValue(Notification.class);
                            if (notif != null) {
                                notif.setId(notifSnapshot.getKey());
                                notificationList.add(notif);
                                userIds.add(notif.getFromUserId());
                            }
                        }

                        // Récupère les noms des utilisateurs ayant envoyé les notifications
                        loadNotificationSenders(userIds);

                        // Vérifie s'il y a des notifications non lues
                        boolean hasUnread = false;
                        for (Notification notif : notificationList) {
                            if (!notif.isRead()) {
                                hasUnread = true;
                                break;
                            }
                        }

                        // Met à jour l'icône de notification
                        ImageButton notifButton = findViewById(R.id.notification_button);
                        if (hasUnread) {
                            notifButton.setImageResource(R.drawable.ic_notification_active);
                        } else {
                            notifButton.setImageResource(R.drawable.ic_notification_inactive);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Erreur chargement des notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Charge les utilisateurs, posts et commentaires liés aux notifications
    private void loadNotificationSenders(List<String> userIds) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments");

        Map<String, String> idToName = new HashMap<>();
        Map<String, Post> postMap = new HashMap<>();
        Map<String, Comment> commentMap = new HashMap<>();

        // Charge les noms des utilisateurs
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    User user = userSnap.getValue(User.class);
                    if (user != null && userIds.contains(user.getUid())) {
                        idToName.put(user.getUid(), user.getFullName());
                    }
                }

                // Charge ensuite les posts
                postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot postSnap) {
                        for (DataSnapshot postNode : postSnap.getChildren()) {
                            Post post = postNode.getValue(Post.class);
                            if (post != null) {
                                postMap.put(post.getId(), post);
                            }
                        }

                        // Charge ensuite les commentaires
                        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot commentSnap) {
                                for (DataSnapshot commentNode : commentSnap.getChildren()) {
                                    Comment comment = commentNode.getValue(Comment.class);
                                    if (comment != null) {
                                        commentMap.put(comment.getPostId(), comment);
                                    }
                                }

                                // Affiche les notifications une fois toutes les données chargées
                                showNotificationsDropdown(idToName, postMap, commentMap);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Erreur chargement commentaires", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Erreur chargement posts", Toast.LENGTH_SHORT).show();
                    }
                });

                // Déjà présent : vérifie et met à jour l'icône
                boolean hasUnread = false;
                for (Notification notif : notificationList) {
                    if (!notif.isRead()) {
                        hasUnread = true;
                        break;
                    }
                }

                ImageButton notifButton = findViewById(R.id.notification_button);
                if (hasUnread) {
                    notifButton.setImageResource(R.drawable.ic_notification_active);
                } else {
                    notifButton.setImageResource(R.drawable.ic_notification_inactive);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erreur chargement noms utilisateurs", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showNotificationsDropdown(Map<String, String> idToName,
                                           Map<String, Post> postMap,
                                           Map<String, Comment> commentMap) {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.notification_button));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.getDefault());

        for (int i = 0; i < notificationList.size(); i++) {
            Notification notif = notificationList.get(i);

            String senderName = idToName.getOrDefault(notif.getFromUserId(), "Utilisateur inconnu");
            String actionText = "";
            String contentPreview = "";
            String dateText = dateFormat.format(new Date(notif.getTimestamp()));

            switch (notif.getType().toLowerCase()) {
                case "like":
                    actionText = senderName + " a aimé votre post.";
                    Post likedPost = postMap.get(notif.getPostId());
                    if (likedPost != null) {
                        contentPreview = "\"" + getShortText(likedPost.getText()) + "\"";
                    }
                    break;

                case "dislike":
                    actionText = senderName + " n'a pas aimé votre post.";
                    Post dislikedPost = postMap.get(notif.getPostId());
                    if (dislikedPost != null) {
                        contentPreview = "\"" + getShortText(dislikedPost.getText()) + "\"";
                    }
                    break;

                case "comment":
                    actionText = senderName + " a commenté votre post.";
                    Comment comment = commentMap.get(notif.getPostId()); // notif.postId pointe vers le post, pas le commentaire directement
                    if (comment != null) {
                        contentPreview = "\"" + getShortText(comment.getText()) + "\"";
                    }
                    break;

                default:
                    actionText = senderName + " a effectué une action : " + notif.getType();
            }

            String finalMessage = actionText + "\n" + contentPreview + "\n" + dateText;
            popup.getMenu().add(0, i, i, finalMessage);
        }

        popup.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            Notification selectedNotif = notificationList.get(index);



            // Marquer comme lue localement
            selectedNotif.setRead(true);

            // Mettre à jour dans Firebase
            DatabaseReference notifRef = FirebaseDatabase.getInstance()
                    .getReference("notifications")
                    .child(selectedNotif.getId());

            notifRef.child("isRead").setValue(true);



            // Affichage du détail
            String senderName = idToName.getOrDefault(selectedNotif.getFromUserId(), "Utilisateur inconnu");
            String actionText = "", contentPreview = "", postText = "", commentText = "";
            String dateText = new SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.getDefault())
                    .format(new Date(selectedNotif.getTimestamp()));

            switch (selectedNotif.getType().toLowerCase()) {
                case "like":
                    actionText = senderName + " a aimé votre post.";
                    Post likedPost = postMap.get(selectedNotif.getPostId());
                    if (likedPost != null) postText = likedPost.getText();
                    break;

                case "dislike":
                    actionText = senderName + " n'a pas aimé votre post.";
                    Post dislikedPost = postMap.get(selectedNotif.getPostId());
                    if (dislikedPost != null) postText = dislikedPost.getText();
                    break;

                case "comment":
                    actionText = senderName + " a commenté votre post.";
                    Comment comment = commentMap.get(selectedNotif.getPostId());
                    if (comment != null) commentText = comment.getText();
                    break;

                default:
                    actionText = senderName + " a effectué une action : " + selectedNotif.getType();
            }

            StringBuilder detailMessage = new StringBuilder();
            detailMessage.append(actionText).append("\n\n");
            if (!postText.isEmpty()) detailMessage.append("Post : ").append(postText).append("\n\n");
            if (!commentText.isEmpty()) detailMessage.append("Commentaire : ").append(commentText).append("\n\n");
            detailMessage.append("Date : ").append(dateText);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Détail de la notification")
                    .setMessage(detailMessage.toString())
                    .setPositiveButton("Fermer", null)
                    .show();

            return true;
        });



        popup.show();
    }

    // Méthode pour tronquer le texte si trop long
    private String getShortText(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.length() > 40 ? text.substring(0, 40) + "..." : text;
    }


}