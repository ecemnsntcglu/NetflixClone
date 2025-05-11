package com.ecs.netflix;

import java.util.List;

public class Content {
    private String id;
    private String title;
    private String posterUrl;
    private String type; // "Film" veya "Dizi"
    private String description;
    private String trailerUrl;
    private String director;
    private List<String> cast;

    public Content() {}

    public Content(String id, String title, String posterUrl, String type) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.type = type;
        this.description = description;
        this.trailerUrl = trailerUrl;
        this.director = director;
        this.cast = cast;
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