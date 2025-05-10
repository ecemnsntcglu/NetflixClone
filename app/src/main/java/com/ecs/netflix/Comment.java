package com.ecs.netflix;

public class Comment {
    private String userName;
    private String commentText;
    private String status;

    public Comment(String userName, String commentText, String status) {
        this.userName = userName;
        this.commentText = commentText;
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getStatus() {
        return status;
    }
}