package com.dev.minisocialapp.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.Comment;
import com.dev.minisocialapp.models.Post;
import com.dev.minisocialapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    // Liste des publications et utilisateurs
private List<Post> posts;
private Context context;

    private List<User> users;

    private String currentUserId;

    // Constructeur
    public PostsAdapter(List<Post> posts, List<User> users, String currentUserId, Context context) {
        this.posts = posts;
        this.users = users;
        this.currentUserId = currentUserId;
        this.context = context;
    }

    // Classe interne représentant la vue d’un seul post
public static class PostViewHolder extends RecyclerView.ViewHolder {
        // Composants d'un post
    TextView username, postTime, postContent;
    ImageView postUserImage, postImage;

    ImageButton likeButton, dislikeButton, commentButton;
    TextView likeCount, dislikeCount, commentCount;

    Button showCommentsButton;
    RecyclerView commentsRecyclerView;
    LinearLayout commentInputLayout;
    EditText commentInput;
    ImageButton sendCommentButton;



    public PostViewHolder(View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.post_username);
        postTime = itemView.findViewById(R.id.post_time);
        postContent = itemView.findViewById(R.id.post_content);
        postUserImage = itemView.findViewById(R.id.post_user_image);
        postImage = itemView.findViewById(R.id.post_image);

        likeButton = itemView.findViewById(R.id.like_button);
        dislikeButton = itemView.findViewById(R.id.dislike_button);
        commentButton = itemView.findViewById(R.id.comment_button);
        likeCount = itemView.findViewById(R.id.like_count);
        dislikeCount = itemView.findViewById(R.id.dislike_count);
        commentCount = itemView.findViewById(R.id.comment_count);

        showCommentsButton = itemView.findViewById(R.id.show_comments_button);
        commentsRecyclerView = itemView.findViewById(R.id.comments_recycler_view);
        commentInputLayout = itemView.findViewById(R.id.comment_input_layout);
        commentInput = itemView.findViewById(R.id.comment_input);
        sendCommentButton = itemView.findViewById(R.id.send_comment_button);

    }
}


  //  Méthode appelée pour créer la vue d'un post à partir du layout item_post.xml
@Override
public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // Création de la vue pour chaque élément de la liste
    View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
    return new PostViewHolder(view);
}


   // méthode principale de liaison des données
    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        // Liaison des données à la vue
            Post post = posts.get(position);
            User postUser = getUserById(post.getUserId());
        String postId = post.getId();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Chargement des réactions et des commentaires

        loadReactions(holder, postId, userId);
        loadCommentsCount(holder, postId);


        handleLikeDislikeClicks(holder, postId, post, userId);


        // Affichage du contenu textuel et de l'heure relative
            holder.postContent.setText(post.getText());
            if (post.getTimestamp() > 0) {
                holder.postTime.setText(getRelativeTime(post.getTimestamp()));
            }

        // Affichage de l’image du post si elle existe
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                holder.postImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(post.getImageUrl()).into(holder.postImage);
            } else {
                holder.postImage.setVisibility(View.GONE);
            }



        // Affichage des informations de l’utilisateur
        if (postUser != null) {
            holder.username.setText(postUser.getFullName() != null ? postUser.getFullName() : "Nom manquant");

            if (postUser.getProfileImageUrl() != null && !postUser.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(postUser.getProfileImageUrl())
                        .into(holder.postUserImage);

            } else {
                holder.postUserImage.setImageResource(R.drawable.ic_profile_default);
            }

        } else {
            holder.username.setText("Utilisateur inconnu");
            holder.postUserImage.setImageResource(R.drawable.ic_profile_default);
        }











        // Gérer l'affichage / masquage des commentaires

        holder.showCommentsButton.setOnClickListener(v -> {
            boolean isVisible = holder.commentsRecyclerView.getVisibility() == View.VISIBLE;

            if (isVisible) {
                holder.commentsRecyclerView.setVisibility(View.GONE);
                holder.commentInputLayout.setVisibility(View.GONE);
                holder.showCommentsButton.setText("voir les commentaires");
            } else {
                holder.commentsRecyclerView.setVisibility(View.VISIBLE);
                holder.commentInputLayout.setVisibility(View.VISIBLE);
                holder.showCommentsButton.setText("masquer les commentaires");

                // Charger les commentaires ici


                getCommentsForPost(post.getId(), comments -> {
                    CommentAdapter commentAdapter = new CommentAdapter(comments);
                    holder.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                    holder.commentsRecyclerView.setAdapter(commentAdapter);
                });
            }
        });






        holder.sendCommentButton.setOnClickListener(v -> {
            String commentText = holder.commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addCommentToPost(post.getId(), currentUserId, commentText, () -> {
                    holder.commentInput.setText(""); // Réinitialiser après envoi
                });
            }
        });









    }





    private void addCommentToPost(String postId, String userId, String commentText, Runnable onSuccess) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("comments").child(postId);
        String commentId = commentRef.push().getKey(); // Génère un ID unique

        if (commentId == null) return;

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("userId", userId);
        commentMap.put("text", commentText);
        commentMap.put("timestamp", System.currentTimeMillis());

        sendNotification(getPostOwnerId(postId), userId , "comment", postId);

        commentRef.child(commentId).setValue(commentMap)
                .addOnSuccessListener(aVoid -> {
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erreur lors de l'ajout du commentaire", Toast.LENGTH_SHORT).show();
                });
    }



    private String getPostOwnerId(String postId) {
        for (Post p : posts) {
            if (p.getId().equals(postId)) {
                return p.getUserId();
            }
        }
        return "";
    }


    private void getCommentsForPost(String postId, CommentAdapter.CommentCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comments").child(postId);

        ref.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();
                Set<String> userIds = new HashSet<>();

                for (DataSnapshot commentSnap : snapshot.getChildren()) {
                    String id = commentSnap.getKey();
                    String userId = commentSnap.child("userId").getValue(String.class);
                    String text = commentSnap.child("text").getValue(String.class);
                    Long timestamp = commentSnap.child("timestamp").getValue(Long.class);

                    Comment comment = new Comment(id, postId, userId, text, timestamp != null ? timestamp : 0);
                    comments.add(comment);

                    if (userId != null) {
                        userIds.add(userId);
                    }
                }

                loadCommentUsers(comments, userIds, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCommentsLoaded(new ArrayList<>());
            }
        });

    }






    private void loadCommentUsers(List<Comment> comments, Set<String> userIds, CommentAdapter.CommentCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, User> userMap = new HashMap<>();

                for (String uid : userIds) {
                    DataSnapshot userSnap = snapshot.child(uid);
                    if (userSnap.exists()) {
                        User user = userSnap.getValue(User.class);
                        if (user != null) {
                            userMap.put(uid, user);
                        }
                    }
                }

                // Associer chaque user au commentaire
                for (Comment comment : comments) {
                    comment.setUser(userMap.get(comment.getUserId()));
                }

                callback.onCommentsLoaded(comments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCommentsLoaded(comments); // continuer sans utilisateurs si erreur
            }
        });
    }









    private void loadCommentsCount(PostViewHolder holder, String postId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comments").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.commentCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }










    private void handleLikeDislikeClicks(PostViewHolder holder, String postId, Post post, String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("reactions").child(postId);

        holder.likeButton.setOnClickListener(v -> {
            ref.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String current = snapshot.getValue(String.class);
                    if ("like".equals(current)) {
                        ref.child(userId).removeValue();

                    } else {
                        ref.child(userId).setValue("like");

                        //notif


                        sendNotification(post.getUserId(), userId, "like", postId);
                    }



                }
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        holder.dislikeButton.setOnClickListener(v -> {
            ref.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String current = snapshot.getValue(String.class);
                    if ("dislike".equals(current)) {
                        ref.child(userId).removeValue();
                    } else {
                        ref.child(userId).setValue("dislike");
                      //notif
                        sendNotification(post.getUserId(), userId, "dislike", postId);
                    }
                }
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }













    private void loadReactions(PostViewHolder holder, String postId, String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("reactions").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int like = 0, dislike = 0;
                String myReaction = null;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String type = ds.getValue(String.class);
                    if ("like".equals(type)) like++;
                    else if ("dislike".equals(type)) dislike++;

                    if (ds.getKey().equals(userId)) myReaction = type;
                }

                holder.likeCount.setText(String.valueOf(like));
                holder.dislikeCount.setText(String.valueOf(dislike));

                holder.likeButton.setColorFilter("like".equals(myReaction) ?
                        ContextCompat.getColor(context, R.color.white) :
                        ContextCompat.getColor(context, R.color.text_secondary));

                holder.dislikeButton.setColorFilter("dislike".equals(myReaction) ?
                        ContextCompat.getColor(context, R.color.white) :
                        ContextCompat.getColor(context, R.color.text_secondary));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }








    private User getUserById(String userId) {
        for (User user : users) {
            if (user.getUid().equals(userId)) {
                return user;
            }
        }
        return null;
    }

@Override
public int getItemCount() {
    return posts.size();
}

private String getRelativeTime(long timestamp) {
    // Formate le temps en "2h ago" etc.
    return DateUtils.getRelativeTimeSpanString(timestamp).toString();
}








    private void sendNotification(String toUserId, String fromUserId, String type, String postId) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");
        String notifId = notifRef.push().getKey();




        Map<String, Object> notifData = new HashMap<>();
        notifData.put("toUserId", toUserId);
        notifData.put("fromUserId", fromUserId);
        notifData.put("type", type); // like, dislike, comment
        notifData.put("postId", postId);
        notifData.put("timestamp", System.currentTimeMillis());
        notifData.put("isRead",  false);




        notifRef.child(notifId).setValue(notifData);
    }





}