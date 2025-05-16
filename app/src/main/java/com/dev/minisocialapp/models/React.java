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
   
}