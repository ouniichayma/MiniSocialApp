package com.dev.minisocialapp.models;

public class Post  {
    private String id;
    private String userId;
    private String text;
    private String imageUrl;
    private long timestamp;

    public Post() {}
    public Post(String id, String userId, String text, String imageUrl, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

}