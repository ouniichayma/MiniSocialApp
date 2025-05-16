package com.dev.minisocialapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.Post;
import com.dev.minisocialapp.models.User;

import java.util.List;
import java.util.Map;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<Post> posts;
    private Map<String, User> usersMap; // Pour récupérer les infos utilisateur (nom, image)
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onLikeClicked(Post post);
        void onDislikeClicked(Post post);
        void onShowCommentsClicked(Post post);
        void onSendComment(Post post, String commentText);
    }

    public PostsAdapter(List<Post> posts, Map<String, User> usersMap, OnPostInteractionListener listener) {
        this.posts = posts;
        this.usersMap = usersMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        User user = usersMap.get(post.getUserId());
        holder.bind(post, user);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage, postImage;
        TextView username, postTime, postContent, likeCount, dislikeCount, commentCount;
        ImageButton likeBtn, dislikeBtn, commentBtn;
        Button showCommentsBtn;
        RecyclerView commentsRecyclerView;
        LinearLayout commentInputLayout;
        EditText commentInput;
        ImageButton sendCommentBtn;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.post_user_image);
            postImage = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.post_username);
            postTime = itemView.findViewById(R.id.post_time);
            postContent = itemView.findViewById(R.id.post_content);
            likeCount = itemView.findViewById(R.id.like_count);
            dislikeCount = itemView.findViewById(R.id.dislike_count);
            commentCount = itemView.findViewById(R.id.comment_count);
            likeBtn = itemView.findViewById(R.id.like_button);
            dislikeBtn = itemView.findViewById(R.id.dislike_button);
            commentBtn = itemView.findViewById(R.id.comment_button);
            showCommentsBtn = itemView.findViewById(R.id.show_comments_button);
            commentsRecyclerView = itemView.findViewById(R.id.comments_recycler_view);
            commentInputLayout = itemView.findViewById(R.id.comment_input_layout);
            commentInput = itemView.findViewById(R.id.comment_input);
            sendCommentBtn = itemView.findViewById(R.id.send_comment_button);
        }

        void bind(Post post, User user) {
            // Afficher infos utilisateur
            username.setText(user != null ? user.getFullName() : "Utilisateur");
            // TODO : charger l’image utilisateur avec Glide/Picasso via user.getProfileImageUrl()

            postContent.setText(post.getText());

            // TODO : Formater timestamp en date relative "2h ago"
            postTime.setText(formatTimestamp(post.getTimestamp()));

            // Gérer image du post
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                postImage.setVisibility(View.VISIBLE);
                // Charger image avec Glide/Picasso
                // Glide.with(postImage.getContext()).load(post.getImageUrl()).into(postImage);
            } else {
                postImage.setVisibility(View.GONE);
            }

            // Exemple: charger le nombre de likes, dislikes, commentaires (à récupérer depuis ta source de données)
            likeCount.setText("24");
            dislikeCount.setText("3");
            commentCount.setText("7");

            // Click listeners
            likeBtn.setOnClickListener(v -> listener.onLikeClicked(post));
            dislikeBtn.setOnClickListener(v -> listener.onDislikeClicked(post));
            showCommentsBtn.setOnClickListener(v -> listener.onShowCommentsClicked(post));
            sendCommentBtn.setOnClickListener(v -> {
                String commentText = commentInput.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    listener.onSendComment(post, commentText);
                    commentInput.setText("");
                }
            });
        }
    }

    private String formatTimestamp(long timestamp) {
        // Implémenter la conversion timestamp -> "2h ago"
        // Exemple simple (non exact) :
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / (60 * 1000);
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }
}