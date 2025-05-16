package com.dev.minisocialapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.minisocialapp.R;
import com.dev.minisocialapp.models.Comment;
import com.dev.minisocialapp.models.User;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    private Context context;

    public CommentAdapter(List<Comment> comments) {
        this.commentList = comments;



    }

    public interface CommentCallback {
        void onCommentsLoaded(List<Comment> comments);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentUserName, commentText, commentTime;
        ImageView userImageView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentUserName = itemView.findViewById(R.id.comment_user_name); // ajoute ça dans ton layout
            commentText = itemView.findViewById(R.id.comment_text);         // ajoute ça aussi
            commentTime = itemView.findViewById(R.id.comment_time);

            userImageView=itemView.findViewById(R.id.comment_imageview);// idem


        }
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.commentUserName.setText(comment.getUserId()); // remplace par le vrai nom si besoin
        holder.commentText.setText(comment.getText());
        holder.commentTime.setText(getRelativeTime(comment.getTimestamp()));




        User user = comment.getUser();
        if (user != null) {
            holder.commentUserName.setText(user.getFullName());
            Log.d("DEBUG", "Comment user loaded: " + user.getFullName());
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImageUrl())
                    .into(holder.userImageView);
        } else {
            holder.commentUserName.setText("Utilisateur inconnu");
            Log.w("DEBUG", "User is null for comment: " + comment.getId());
        }



    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private String getRelativeTime(long timestamp) {
        // Implémentation simple, remplace par quelque chose de plus précis si besoin
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        if (minutes < 60) return minutes + " min";
        long hours = minutes / 60;
        if (hours < 24) return hours + " h";
        long days = hours / 24;
        return days + " j";
    }
}