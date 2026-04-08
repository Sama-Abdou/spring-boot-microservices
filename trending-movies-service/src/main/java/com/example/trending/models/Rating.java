package main.java.com.example.trending.models;

import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

@Entity
@Table(name = "ratings") // must match the table name you created in Task A
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String movieId;
    private Integer rating;

    public String getMovieId() { return movieId; }
    public Integer getRating() { return rating; }
} 