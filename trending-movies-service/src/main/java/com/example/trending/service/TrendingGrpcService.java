package com.example.trending.service;

import com.example.trending.models.Rating;
import com.example.trending.models.UserRating;

import com.example.grpc.tmsrvc.TrendingServiceGrpc;
import com.example.grpc.tmsrvc.TrendingMoviesRequest;
import com.example.grpc.tmsrvc.TrendingMoviesResponse;
import com.example.grpc.tmsrvc.TopRatedMovie;

// import com.example.trending.repository.RatingRepository;
import org.springframework.web.client.RestTemplate;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService // endpt for grpc
public class TrendingGrpcService extends TrendingServiceGrpc.TrendingServiceImplBase {

    // @Autowired
    // private RatingRepository ratingRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
public void getTrendingMovies(TrendingMoviesRequest request, StreamObserver<TrendingMoviesResponse> responseObserver) {
    // 1. Get the 'N' from the request (default to 10 if not provided)
    int topN = request.getTopRating() > 0 ? request.getTopRating() : 10;

    try {
        // 2. Fetch data
        UserRating userRating = restTemplate.getForObject(
                "http://ratings-data-service/ratingsdata/users/1", // Use Service Name
                UserRating.class);

        if (userRating != null && userRating.getRatings() != null) {
            TrendingMoviesResponse.Builder responseBuilder = TrendingMoviesResponse.newBuilder();

            // 3. Sort and Build
            userRating.getRatings().stream()
                .sorted((a, b) -> Integer.compare(b.getRating(), a.getRating())) // Assuming rating is int
                .limit(topN)
                .forEach(r -> {
                    TopRatedMovie movie = TopRatedMovie.newBuilder()
                            .setMovieId(r.getMovieId())
                            .setRating(r.getRating())
                            .build();
                    responseBuilder.addMovies(movie);
                });

            responseObserver.onNext(responseBuilder.build());
        }
    } catch (Exception e) {
        // Log the error so you can see it in the console if the call fails
        System.err.println("Error calling Ratings Service: " + e.getMessage());
    } finally {
        responseObserver.onCompleted();
    }
}
}