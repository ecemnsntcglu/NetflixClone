package com.ecs.netflix;

import java.util.List;

public class Dizi {
    private String id; // Firestore'daki belge ID'si
    private String title;
    private String director;
    private int episodes;
    private String releaseDate;
    private double rating;
    private String type;
    private String trailer_url;
    private String poster_url;
    private String description;
    private List<String> cast;
    private List<String> genres;

    public Dizi() {
        // Firebase için boş constructor gerekli
    }

    public Dizi(String id, String title, String director, int episodes, String releaseDate, double rating,
                String type, String trailerUrl, String posterUrl, String description,
                List<String> cast, List<String> genres) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.episodes = episodes;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.type = type;
        this.trailer_url = trailerUrl;
        this.poster_url = posterUrl;
        this.description = description;
        this.cast = cast;
        this.genres = genres;
    }

    // Getter metotları
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDirector() { return director; }
    public int getEpisodes() { return episodes; }
    public String getReleaseDate() { return releaseDate; }
    public double getRating() { return rating; }
    public String getType() { return type; }
    public String getTrailer_url() { return trailer_url; }
    public String getPoster_url() { return poster_url; }
    public String getDescription() { return description; }
    public List<String> getCast() { return cast; }
    public List<String> getGenres() { return genres; }

    // Setter metotları
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosterUrl(String posterUrl) {
        this.poster_url = posterUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailer_url = trailerUrl;
    }
}