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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.Post;
import com.dev.minisocialapp.models.User;

import java.util.List;
import java.util.Map;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

private List<Post> posts;
private Context context;




    private List<User> users;

    private String currentUserId;

    public PostsAdapter(List<Post> posts, List<User> users, String currentUserId, Context context) {
        this.posts = posts;
        this.users = users;
        this.currentUserId = currentUserId;
        this.context = context;
    }

public static class PostViewHolder extends RecyclerView.ViewHolder {
    TextView username, postTime, postContent;
    ImageView postUserImage, postImage;

    public PostViewHolder(View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.post_username);
        postTime = itemView.findViewById(R.id.post_time);
        postContent = itemView.findViewById(R.id.post_content);
        postUserImage = itemView.findViewById(R.id.post_user_image);
        postImage = itemView.findViewById(R.id.post_image);
    }
}

@Override
public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
    return new PostViewHolder(view);
}

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {

            Post post = posts.get(position);
            User postUser = getUserById(post.getUserId());

            if (postUser != null) {
                holder.username.setText(postUser.getFullName());
                Glide.with(context).load(postUser.getProfileImageUrl()).into(holder.postUserImage);
            } else {
                holder.username.setText("Utilisateur inconnu");
                Glide.with(context).load(R.drawable.ic_profile_default).into(holder.postUserImage);
            }

            holder.postContent.setText(post.getText());

            if (post.getTimestamp() > 0) {
                holder.postTime.setText(getRelativeTime(post.getTimestamp()));
            }

            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                holder.postImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(post.getImageUrl()).into(holder.postImage);
            } else {
                holder.postImage.setVisibility(View.GONE);
            }




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
}