package com.dev.minisocialapp.models;

public class Comment {
    private String id;
    private String postId;
    private String userId;
    private String text;
    private long timestamp;

    public Comment() {}
    public Comment(String id, String postId, String userId, String text, long timestamp) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }

}