package com.ecs.netflix;

import java.util.List;

public class Film {
    private String title;
    private String director;
    private int duration;
    private String releaseDate;
    private double rating;
    private String type;
    private String trailerUrl;
    private String posterUrl;
    private String description;
    private List<String> cast;
    private List<String> genres;

    // Boş constructor (Firestore için gereklidir)
    public Film() {}

    // Parametreli constructor
    public Film(String title, String director, int duration, String releaseDate, double rating,
                String type, String trailerUrl, String posterUrl, String description,
                List<String> cast, List<String> genres) {
        this.title = title;
        this.director = director;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.type = type;
        this.trailerUrl = trailerUrl;
        this.posterUrl = posterUrl;
        this.description = description;
        this.cast = cast;
        this.genres = genres;
    }

    // Getter & Setter metotları
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

    public String getTrailer_url() { return trailerUrl; }
    public void setTrailer_url(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public String getPoster_url() { return posterUrl; }
    public void setPoster_url(String posterUrl) { this.posterUrl = posterUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}
