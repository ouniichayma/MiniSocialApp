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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}