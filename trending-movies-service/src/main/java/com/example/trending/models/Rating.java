package com.example.trending.models;

public class Rating {
    private String movieId;
    private int rating;

    public String getMovieId() { return movieId; }
    public int getRating() { return rating; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public void setRating(int rating) { this.rating = rating; }
}