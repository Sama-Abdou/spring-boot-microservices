package com.example.movieinfoservice.models;

//
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies") //telling Mongo the collection name
public class Movie {

    @Id //so Mongo knows 'movieId' is the unique key
    private String movieId;
    private String name;
    private String description;

    public Movie() {
    }

    public Movie(String movieId, String name, String description) {
        this.movieId = movieId;
        this.name = name;
        this.description = description;
    }

    //nth new
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}