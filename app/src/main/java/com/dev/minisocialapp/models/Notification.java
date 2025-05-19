package com.dev.minisocialapp.models;

public class Notification {

    private String id;
    private String toUserId;
    private String fromUserId;
    private String type; // like, comment, dislike

    private String postId;
    private long timestamp;
    private boolean isRead;

    public Notification() {} // Firebase

    public Notification(String id, String toUserId, String fromUserId, String type, String postId, long timestamp, boolean isRead) {
        this.id = id;
        this.toUserId = toUserId;
        this.fromUserId = fromUserId;
        this.type = type;
        this.postId = postId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
