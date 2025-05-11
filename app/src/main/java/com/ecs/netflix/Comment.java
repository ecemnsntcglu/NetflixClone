package com.ecs.netflix;

public class Comment {
    private String userId;
    private String commentText;
    private long timestamp;

    public Comment() {}

    public Comment(String userId, String commentText, long timestamp) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getCommentText() { return commentText; }
    public long getTimestamp() { return timestamp; }
}
