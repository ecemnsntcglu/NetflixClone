package com.ecs.netflix;

public class Comment {
    private String userId;
    private String commentText;
    private String type;
    private String status;
    private long timestamp;

    // 🔥 Boş Constructor (Firestore için gereklidir)
    public Comment() {}

    // 🔥 Tüm alanları içeren Constructor
    public Comment(String userId, String commentText, String type, String status, long timestamp) {
        this.userId = userId;
        this.commentText = commentText;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    // 🔥 Getter ve Setter Metodları
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}