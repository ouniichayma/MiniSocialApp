package com.dev.minisocialapp.models;

public class React {
    private String id;
    private String postId;
    private String userId;
    private String type; // "like" ou "dislike"

    public React() {}
    public React(String id, String postId, String userId, String type) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.type = type;
    }


    public String getId() { return id; }
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }

    public void setId(String id) { this.id = id; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
   
}