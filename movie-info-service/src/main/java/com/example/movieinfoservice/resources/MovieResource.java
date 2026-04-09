package com.example.movieinfoservice.resources;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; //repo
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.movieinfoservice.models.Movie;
import com.example.movieinfoservice.models.MovieRepository;
import com.example.movieinfoservice.models.MovieSummary;

@RestController
@RequestMapping("/movies")
public class MovieResource {

    @Value("${api.key}")
    private String apiKey;

    private RestTemplate restTemplate;

    //injecting the Repository
    @Autowired
    private MovieRepository movieRepository;

    public MovieResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId) throws InterruptedException {
        
        //1: Check MongoDB Cache First
        Optional<Movie> cachedMovie = movieRepository.findById(movieId);
        
        if (cachedMovie.isPresent()) {
            System.out.println("CACHE HIT: Returning movie " + movieId + " from MongoDB");
            return cachedMovie.get();
        }

        //2: Cache Miss - Get from TMDB
        System.out.println("CACHE MISS: Fetching movie " + movieId + " from TMDB (Simulating delay)...");
        
        //simulating the delay of a "slow" source
        Thread.sleep(2000); 

        final String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;
        MovieSummary movieSummary = restTemplate.getForObject(url, MovieSummary.class);
        
        Movie movie = new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());

        //3: Save to MongoDB for next time
        movieRepository.save(movie);

        return movie;
    }
}