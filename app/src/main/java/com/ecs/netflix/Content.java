package com.ecs.netflix;

public class Content {
    private String id;
    private String title;
    private String posterUrl;
    private String type; // "Film" veya "Dizi"

    public Content() {}

    public Content(String id, String title, String posterUrl, String type) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}