package com.ecs.netflix;

import java.util.List;

public class Film {
    private String id; // Firestore'daki belge ID'si
    private String title;
    private String director;
    private int duration;
    private String releaseDate;
    private double rating;
    private String type;
    private String trailer_url;
    private String poster_url;
    private String description;
    private List<String> cast;
    private List<String> genres;

    // Boş constructor (Firestore için gereklidir)
    public Film() {}

    // Parametreli constructor
    public Film(String id, String title, String director, int duration, String releaseDate, double rating,
                String type, String trailer_url, String poster_url, String description,
                List<String> cast, List<String> genres) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.type = type;
        this.trailer_url = trailer_url;
        this.poster_url = poster_url;
        this.description = description;
        this.cast = cast;
        this.genres = genres;
    }

    // Getter & Setter metotları
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTrailer_url() { return trailer_url; }
    public void setTrailer_url(String trailer_url) { this.trailer_url = trailer_url; }

    public String getPoster_url() { return poster_url; }
    public void setPoster_url(String poster_url) { this.poster_url = poster_url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}